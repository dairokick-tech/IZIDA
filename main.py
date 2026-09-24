
from fastapi import FastAPI, HTTPException, Header
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from pydantic import BaseModel, Field
from pathlib import Path
import sqlite3, hashlib, secrets, datetime, json, math

BASE=Path(__file__).resolve().parent.parent
DB=BASE/"izida.db"
app=FastAPI(title="IZIDA - Billetera Digital Independiente", version="2.0")
app.add_middleware(CORSMiddleware,allow_origins=["*"],allow_credentials=True,allow_methods=["*"],allow_headers=["*"])

def db():
    c=sqlite3.connect(DB); c.row_factory=sqlite3.Row; return c
def hp(v): return hashlib.sha256(v.encode()).hexdigest()
def now(): return datetime.datetime.now().isoformat(timespec="seconds")

def init():
    c=db()
    c.executescript("""
    CREATE TABLE IF NOT EXISTS admins(id INTEGER PRIMARY KEY,username TEXT UNIQUE,password TEXT,created_at TEXT);
    CREATE TABLE IF NOT EXISTS clients(id INTEGER PRIMARY KEY,name TEXT,document TEXT UNIQUE,phone TEXT UNIQUE,email TEXT,username TEXT UNIQUE,password TEXT,status TEXT DEFAULT 'ACTIVO',created_at TEXT);
    CREATE TABLE IF NOT EXISTS wallets(id INTEGER PRIMARY KEY,client_id INTEGER UNIQUE,balance REAL DEFAULT 0,currency TEXT DEFAULT 'PEN',status TEXT DEFAULT 'ACTIVA',created_at TEXT);
    CREATE TABLE IF NOT EXISTS movements(id INTEGER PRIMARY KEY,wallet_id INTEGER,type TEXT,amount REAL,balance_after REAL,description TEXT,reference TEXT UNIQUE,created_at TEXT);
    CREATE TABLE IF NOT EXISTS transfers(id INTEGER PRIMARY KEY,from_wallet INTEGER,to_wallet INTEGER,amount REAL,reference TEXT UNIQUE,status TEXT,description TEXT,created_at TEXT);
    CREATE TABLE IF NOT EXISTS credits(id INTEGER PRIMARY KEY,client_id INTEGER,amount REAL,rate REAL DEFAULT 0,term_months INTEGER DEFAULT 12,status TEXT DEFAULT 'PENDIENTE',installment REAL DEFAULT 0,remaining REAL DEFAULT 0,created_at TEXT,approved_at TEXT);
    CREATE TABLE IF NOT EXISTS credit_payments(id INTEGER PRIMARY KEY,credit_id INTEGER,amount REAL,reference TEXT UNIQUE,created_at TEXT);
    CREATE TABLE IF NOT EXISTS campaigns(id INTEGER PRIMARY KEY,name TEXT,description TEXT,min_balance REAL DEFAULT 0,rate REAL DEFAULT 0,term_months INTEGER DEFAULT 12,active INTEGER DEFAULT 1,created_at TEXT);
    CREATE TABLE IF NOT EXISTS investments(id INTEGER PRIMARY KEY,client_id INTEGER,amount REAL,currency TEXT,status TEXT DEFAULT 'ACTIVA',rate REAL DEFAULT 0,maturity_date TEXT,created_at TEXT);
    CREATE TABLE IF NOT EXISTS payments(id INTEGER PRIMARY KEY,from_wallet INTEGER,to_wallet INTEGER,amount REAL,concept TEXT,reference TEXT UNIQUE,status TEXT,created_at TEXT);
    CREATE TABLE IF NOT EXISTS receipts(id INTEGER PRIMARY KEY,reference TEXT UNIQUE,payload TEXT,created_at TEXT);
    CREATE TABLE IF NOT EXISTS sessions(token TEXT PRIMARY KEY,role TEXT,user_id INTEGER,created_at TEXT);
    CREATE TABLE IF NOT EXISTS audit(id INTEGER PRIMARY KEY,role TEXT,user_id INTEGER,action TEXT,detail TEXT,created_at TEXT);
    """)
    if not c.execute("SELECT 1 FROM admins WHERE username='admin'").fetchone():
        c.execute("INSERT INTO admins(username,password,created_at) VALUES(?,?,?)",("admin",hp("IZIDA2026"),now()))
    c.commit(); c.close()
init()

class Login(BaseModel): username:str; password:str
class Register(BaseModel): name:str; document:str; phone:str; email:str=""; username:str; password:str
class Amount(BaseModel): amount:float=Field(gt=0); description:str=""
class TransferIn(BaseModel): destination_username:str; amount:float=Field(gt=0); description:str=""
class CreditIn(BaseModel): client_id:int; amount:float=Field(gt=0); rate:float=0; term_months:int=12
class CampaignIn(BaseModel): name:str; description:str=""; min_balance:float=0; rate:float=0; term_months:int=12
class InvestmentIn(BaseModel): amount:float=Field(gt=0); currency:str; rate:float=0; term_days:int=30
class PayCreditIn(BaseModel): amount:float=Field(gt=0)
class PayIn(BaseModel): destination_username:str; amount:float=Field(gt=0); concept:str="Pago IZIDA"

def token(role,uid):
    t=secrets.token_urlsafe(32); c=db()
    c.execute("INSERT INTO sessions VALUES(?,?,?,?)",(t,role,uid,now())); c.commit(); c.close(); return t
def auth(header,role):
    t=(header or "").replace("Bearer ",""); c=db(); r=c.execute("SELECT * FROM sessions WHERE token=?",(t,)).fetchone(); c.close()
    if not r or r["role"]!=role: raise HTTPException(401,"Sesión requerida")
    return r
def audit(role,uid,action,detail=""):
    c=db(); c.execute("INSERT INTO audit(role,user_id,action,detail,created_at) VALUES(?,?,?,?,?)",(role,uid,action,detail,now())); c.commit(); c.close()
def ref(prefix):
    return prefix+"-"+secrets.token_hex(6).upper()
def wallet(c,cid):
    return c.execute("SELECT * FROM wallets WHERE client_id=?",(cid,)).fetchone()
def add_move(c,wid,typ,amt,desc):
    w=c.execute("SELECT balance FROM wallets WHERE id=?",(wid,)).fetchone()
    if not w: raise HTTPException(404,"Billetera no encontrada")
    nb=round(w["balance"]+amt,2)
    if nb < -0.001: raise HTTPException(400,"Saldo insuficiente")
    c.execute("UPDATE wallets SET balance=? WHERE id=?",(nb,wid))
    r=ref("IZI")
    c.execute("INSERT INTO movements(wallet_id,type,amount,balance_after,description,reference,created_at) VALUES(?,?,?,?,?,?,?)",(wid,typ,amt,nb,desc,r,now()))
    return r,nb

@app.get("/api/health")
def health(): return {"ok":True,"system":"IZIDA","mode":"independiente","external_integrations":False}

@app.post("/api/admin/login")
def admin_login(x:Login):
    c=db(); r=c.execute("SELECT * FROM admins WHERE username=? AND password=?",(x.username,hp(x.password))).fetchone(); c.close()
    if not r: raise HTTPException(401,"Usuario o contraseña incorrectos")
    t=token("admin",r["id"]); audit("admin",r["id"],"LOGIN")
    return {"token":t,"role":"admin","username":r["username"]}

@app.post("/api/client/register")
def register(x:Register):
    c=db()
    try:
        q=c.execute("INSERT INTO clients(name,document,phone,email,username,password,created_at) VALUES(?,?,?,?,?,?,?)",(x.name,x.document,x.phone,x.email,x.username,hp(x.password),now()))
        cid=q.lastrowid; c.execute("INSERT INTO wallets(client_id,created_at) VALUES(?,?)",(cid,)); c.commit()
    except sqlite3.IntegrityError:
        c.close(); raise HTTPException(400,"Documento, teléfono o usuario ya registrado")
    c.close(); return {"ok":True,"message":"Cuenta IZIDA creada","client_id":cid}

@app.post("/api/client/login")
def client_login(x:Login):
    c=db(); r=c.execute("SELECT * FROM clients WHERE username=? AND password=? AND status='ACTIVO'",(x.username,hp(x.password))).fetchone(); c.close()
    if not r: raise HTTPException(401,"Usuario o contraseña incorrectos")
    t=token("client",r["id"]); audit("client",r["id"],"LOGIN")
    return {"token":t,"role":"client","client_id":r["id"],"name":r["name"],"username":r["username"]}

def client_home(cid):
    c=db(); cl=c.execute("SELECT id,name,document,phone,email,username,status,created_at FROM clients WHERE id=?",(cid,)).fetchone()
    w=wallet(c,cid)
    m=c.execute("SELECT * FROM movements WHERE wallet_id=? ORDER BY id DESC LIMIT 50",(w["id"],)).fetchall()
    cr=c.execute("SELECT * FROM credits WHERE client_id=? ORDER BY id DESC",(cid,)).fetchall()
    inv=c.execute("SELECT * FROM investments WHERE client_id=? ORDER BY id DESC",(cid,)).fetchall()
    camps=c.execute("SELECT * FROM campaigns WHERE active=1 AND ? >= min_balance ORDER BY id DESC",(w["balance"],)).fetchall()
    c.close()
    return {"client":dict(cl),"wallet":dict(w),"movements":[dict(x) for x in m],"credits":[dict(x) for x in cr],"investments":[dict(x) for x in inv],"campaigns":[dict(x) for x in camps]}

@app.get("/api/client/home")
def home(authorization:str|None=Header(None)): return client_home(auth(authorization,"client")["user_id"])

@app.get("/api/client/profile")
def profile(authorization:str|None=Header(None)):
    return client_home(auth(authorization,"client")["user_id"])["client"]

@app.post("/api/client/deposit")
def deposit(x:Amount,authorization:str|None=Header(None)):
    s=auth(authorization,"client"); c=db(); w=wallet(c,s["user_id"]); r,b=add_move(c,w["id"],"ABONO",x.amount,x.description or "Abono interno IZIDA"); c.commit(); c.close(); audit("client",s["user_id"],"ABONO",r); return {"ok":True,"reference":r,"balance":b}

@app.post("/api/client/withdraw")
def withdraw(x:Amount,authorization:str|None=Header(None)):
    s=auth(authorization,"client"); c=db(); w=wallet(c,s["user_id"]); r,b=add_move(c,w["id"],"RETIRO",-x.amount,x.description or "Retiro interno IZIDA"); c.commit(); c.close(); audit("client",s["user_id"],"RETIRO",r); return {"ok":True,"reference":r,"balance":b}

@app.post("/api/client/transfer")
def transfer(x:TransferIn,authorization:str|None=Header(None)):
    s=auth(authorization,"client"); c=db(); a=wallet(c,s["user_id"]); b=c.execute("SELECT w.* FROM wallets w JOIN clients cl ON cl.id=w.client_id WHERE cl.username=? AND cl.status='ACTIVO'",(x.destination_username,)).fetchone()
    if not b: c.close(); raise HTTPException(404,"Destinatario no encontrado")
    if a["id"]==b["id"]: c.close(); raise HTTPException(400,"No puede transferirse a sí mismo")
    if a["balance"]<x.amount: c.close(); raise HTTPException(400,"Saldo insuficiente")
    r=ref("TRF"); c.execute("UPDATE wallets SET balance=balance-? WHERE id=?",(x.amount,a["id"])); c.execute("UPDATE wallets SET balance=balance+? WHERE id=?",(x.amount,b["id"]))
    c.execute("INSERT INTO transfers(from_wallet,to_wallet,amount,reference,status,description,created_at) VALUES(?,?,?,?,?,?,?)",(a["id"],b["id"],x.amount,r,"COMPLETADA",x.description or "Transferencia IZIDA",now()))
    c.execute("INSERT INTO movements(wallet_id,type,amount,balance_after,description,reference,created_at) SELECT id,'ENVIO',-?,balance,?,?,? FROM wallets WHERE id=?",(x.amount,x.description or "Transferencia enviada",r,now(),a["id"]))
    c.execute("INSERT INTO movements(wallet_id,type,amount,balance_after,description,reference,created_at) SELECT id,'RECEPCION',?,balance,?,?,? FROM wallets WHERE id=?",(x.amount,x.description or "Transferencia recibida",r,now(),b["id"]))
    c.commit(); c.close(); audit("client",s["user_id"],"TRANSFERENCIA",r); return {"ok":True,"reference":r}

@app.post("/api/client/pay")
def pay(x:PayIn,authorization:str|None=Header(None)):
    return transfer(TransferIn(destination_username=x.destination_username,amount=x.amount,description=x.concept),authorization)

@app.post("/api/client/credit-payment")
def credit_payment(x:PayCreditIn,authorization:str|None=Header(None)):
    s=auth(authorization,"client"); c=db(); cr=c.execute("SELECT * FROM credits WHERE client_id=? AND status='APROBADO' AND remaining>0 ORDER BY id LIMIT 1",(s["user_id"],)).fetchone()
    if not cr: c.close(); raise HTTPException(404,"No tienes crédito pendiente")
    w=wallet(c,s["user_id"])
    if w["balance"]<x.amount: c.close(); raise HTTPException(400,"Saldo insuficiente")
    amt=min(x.amount,cr["remaining"]); r,b=add_move(c,w["id"],"PAGO_CREDITO",-amt,"Pago de crédito IZIDA")
    rem=round(cr["remaining"]-amt,2); status="PAGADO" if rem<=0 else "APROBADO"
    c.execute("UPDATE credits SET remaining=?,status=? WHERE id=?",(rem,status,cr["id"]))
    c.execute("INSERT INTO credit_payments(credit_id,amount,reference,created_at) VALUES(?,?,?,?)",(cr["id"],amt,r,now()))
    c.commit(); c.close(); return {"ok":True,"reference":r,"remaining":rem,"balance":b}

@app.post("/api/client/investment")
def investment(x:InvestmentIn,authorization:str|None=Header(None)):
    s=auth(authorization,"client"); cur=x.currency.upper()
    if cur not in ("PEN","USD"): raise HTTPException(400,"Solo PEN o USD")
    c=db(); w=wallet(c,s["user_id"])
    if cur=="PEN":
        if w["balance"]<x.amount: c.close(); raise HTTPException(400,"Saldo PEN insuficiente")
        r,b=add_move(c,w["id"],"INVERSION",-x.amount,"Inversión IZIDA PEN")
    else: r=None; b=w["balance"]
    maturity=(datetime.datetime.now()+datetime.timedelta(days=x.term_days)).date().isoformat()
    c.execute("INSERT INTO investments(client_id,amount,currency,status,rate,maturity_date,created_at) VALUES(?,?,?,'ACTIVA',?,?,?)",(s["user_id"],x.amount,cur,x.rate,maturity,now()))
    c.commit(); c.close(); return {"ok":True,"currency":cur,"balance_pen":b,"maturity_date":maturity}

@app.get("/api/client/receipt/{reference}")
def receipt(reference:str,authorization:str|None=Header(None)):
    s=auth(authorization,"client"); c=db(); m=c.execute("SELECT m.*,w.client_id FROM movements m JOIN wallets w ON w.id=m.wallet_id WHERE m.reference=?",(reference,)).fetchone()
    if not m or m["client_id"]!=s["user_id"]: c.close(); raise HTTPException(404,"Comprobante no encontrado")
    payload={"reference":reference,"type":m["type"],"amount":m["amount"],"description":m["description"],"date":m["created_at"]}
    c.close(); return payload

@app.get("/api/admin/dashboard")
def dashboard(authorization:str|None=Header(None)):
    sess=auth(authorization,"admin"); c=db()
    d={"clientes":c.execute("SELECT COUNT(*) n FROM clients").fetchone()["n"],"billeteras":c.execute("SELECT COUNT(*) n FROM wallets").fetchone()["n"],"saldo_pen":c.execute("SELECT COALESCE(SUM(balance),0)n FROM wallets").fetchone()["n"],"creditos_aprobados":c.execute("SELECT COALESCE(SUM(amount),0)n FROM credits WHERE status IN ('APROBADO','PAGADO')").fetchone()["n"],"cartera_pendiente":c.execute("SELECT COALESCE(SUM(remaining),0)n FROM credits WHERE status='APROBADO'").fetchone()["n"],"inversiones_pen":c.execute("SELECT COALESCE(SUM(amount),0)n FROM investments WHERE currency='PEN'").fetchone()["n"],"inversiones_usd":c.execute("SELECT COALESCE(SUM(amount),0)n FROM investments WHERE currency='USD'").fetchone()["n"],"movimientos":c.execute("SELECT COUNT(*) n FROM movements").fetchone()["n"]}
    c.close(); return d

@app.get("/api/admin/clients")
def clients(authorization:str|None=Header(None)):
    auth(authorization,"admin"); c=db(); r=c.execute("SELECT id,name,document,phone,email,username,status,created_at FROM clients ORDER BY id DESC").fetchall(); c.close(); return [dict(x) for x in r]

@app.get("/api/admin/wallets")
def wallets(authorization:str|None=Header(None)):
    auth(authorization,"admin"); c=db(); r=c.execute("SELECT w.*,cl.name,cl.username FROM wallets w JOIN clients cl ON cl.id=w.client_id ORDER BY w.id DESC").fetchall(); c.close(); return [dict(x) for x in r]

@app.get("/api/admin/movements")
def movements(authorization:str|None=Header(None)):
    auth(authorization,"admin"); c=db(); r=c.execute("SELECT m.*,cl.name,cl.username FROM movements m JOIN wallets w ON w.id=m.wallet_id JOIN clients cl ON cl.id=w.client_id ORDER BY m.id DESC LIMIT 200").fetchall(); c.close(); return [dict(x) for x in r]

@app.post("/api/admin/credits")
def create_credit(x:CreditIn,authorization:str|None=Header(None)):
    s=auth(authorization,"admin"); c=db()
    if not c.execute("SELECT 1 FROM clients WHERE id=?",(x.client_id,)).fetchone(): c.close(); raise HTTPException(404,"Cliente no existe")
    # Cuota simple de capital + interés anual prorrateado por plazo.
    total=round(x.amount*(1+x.rate/100*x.term_months/12),2); installment=round(total/x.term_months,2)
    c.execute("INSERT INTO credits(client_id,amount,rate,term_months,status,installment,remaining,created_at) VALUES(?,?,?,?,?,?,?,?)",(x.client_id,x.amount,x.rate,x.term_months,"PENDIENTE",installment,total,now())); c.commit(); c.close(); return {"ok":True,"total":total,"installment":installment}

@app.get("/api/admin/credits")
def credits(authorization:str|None=Header(None)):
    auth(authorization,"admin"); c=db(); r=c.execute("SELECT cr.*,cl.name,cl.username FROM credits cr JOIN clients cl ON cl.id=cr.client_id ORDER BY cr.id DESC").fetchall(); c.close(); return [dict(x) for x in r]

@app.post("/api/admin/credits/{cid}/approve")
def approve(cid:int,authorization:str|None=Header(None)):
    s=auth(authorization,"admin"); c=db(); cr=c.execute("SELECT * FROM credits WHERE id=?",(cid,)).fetchone()
    if not cr: c.close(); raise HTTPException(404,"Crédito no encontrado")
    if cr["status"]!="PENDIENTE": c.close(); raise HTTPException(400,"El crédito ya fue procesado")
    w=wallet(c,cr["client_id"]); r,b=add_move(c,w["id"],"DESEMBOLSO_CREDITO",cr["amount"],f"Crédito #{cid}")
    c.execute("UPDATE credits SET status='APROBADO',approved_at=? WHERE id=?",(now(),cid)); c.commit(); c.close(); audit("admin",s["user_id"],"APROBAR_CREDITO",str(cid)); return {"ok":True,"reference":r,"balance":b}

@app.get("/api/admin/campaigns")
def campaigns(authorization:str|None=Header(None)):
    auth(authorization,"admin"); c=db(); r=c.execute("SELECT * FROM campaigns ORDER BY id DESC").fetchall(); c.close(); return [dict(x) for x in r]

@app.post("/api/admin/campaigns")
def create_campaign(x:CampaignIn,authorization:str|None=Header(None)):
    auth(authorization,"admin"); c=db(); c.execute("INSERT INTO campaigns(name,description,min_balance,rate,term_months,active,created_at) VALUES(?,?,?,?,?,?,?)",(x.name,x.description,x.min_balance,x.rate,x.term_months,1,now())); c.commit(); c.close(); return {"ok":True}

@app.get("/api/admin/investments")
def investments(authorization:str|None=Header(None)):
    auth(authorization,"admin"); c=db(); r=c.execute("SELECT i.*,cl.name,cl.username FROM investments i JOIN clients cl ON cl.id=i.client_id ORDER BY i.id DESC").fetchall(); c.close(); return [dict(x) for x in r]

@app.get("/api/admin/risk/{cid}")
def risk(cid:int,authorization:str|None=Header(None)):
    auth(authorization,"admin"); c=db(); cl=c.execute("SELECT id,name,username FROM clients WHERE id=?",(cid,)).fetchone(); w=wallet(c,cid)
    if not cl: c.close(); raise HTTPException(404,"Cliente no encontrado")
    movements=c.execute("SELECT COUNT(*) n FROM movements WHERE wallet_id=?",(w["id"],)).fetchone()["n"]
    sent=c.execute("SELECT COALESCE(SUM(-amount),0)n FROM movements WHERE wallet_id=? AND type='ENVIO' AND amount<0",(w["id"],)).fetchone()["n"]
    received=c.execute("SELECT COALESCE(SUM(amount),0)n FROM movements WHERE wallet_id=? AND type='RECEPCION'",(w["id"],)).fetchone()["n"]
    credits=c.execute("SELECT COUNT(*) n FROM credits WHERE client_id=?",(cid,)).fetchone()["n"]
    paid=c.execute("SELECT COUNT(*) n FROM credits WHERE client_id=? AND status='PAGADO'",(cid,)).fetchone()["n"]
    inv=c.execute("SELECT COUNT(*) n FROM investments WHERE client_id=?",(cid,)).fetchone()["n"]; c.close()
    return {"cliente":dict(cl),"saldo_pen":w["balance"],"movimientos":movements,"enviados":sent,"recibidos":received,"creditos":credits,"creditos_pagados":paid,"inversiones":inv,"fuentes_externas":{"SBS":False,"Sentinel":False}}

@app.get("/api/admin/audit")
def audit_log(authorization:str|None=Header(None)):
    auth(authorization,"admin"); c=db(); r=c.execute("SELECT * FROM audit ORDER BY id DESC LIMIT 200").fetchall(); c.close(); return [dict(x) for x in r]

@app.post("/api/logout")
def logout(authorization:str|None=Header(None)):
    t=(authorization or "").replace("Bearer ",""); c=db(); c.execute("DELETE FROM sessions WHERE token=?",(t,)); c.commit(); c.close(); return {"ok":True}

@app.get("/cliente")
def cliente(): return FileResponse(BASE/"cliente/index.html")
@app.get("/admin")
def admin(): return FileResponse(BASE/"admin/index.html")
@app.get("/")
def root(): return FileResponse(BASE/"cliente/index.html")
