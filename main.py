import os,secrets
from datetime import datetime,timedelta
from fastapi import FastAPI,Depends,HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel,Field
from sqlalchemy import create_engine,Column,Integer,String,Float,DateTime,Text
from sqlalchemy.orm import declarative_base,sessionmaker,Session
DB=os.getenv("DATABASE_URL","sqlite:///./izida.db")
engine=create_engine(DB,connect_args={"check_same_thread":False} if DB.startswith("sqlite") else {})
SessionLocal=sessionmaker(bind=engine);Base=declarative_base()
class Client(Base):
 __tablename__="clients";id=Column(Integer,primary_key=True);document=Column(String(30),unique=True,nullable=False);name=Column(String(160),nullable=False);phone=Column(String(40),default="");email=Column(String(160),default="");username=Column(String(80),unique=True,nullable=False);pin_hash=Column(String(255),default="");status=Column(String(20),default="ACTIVE");created_at=Column(DateTime,default=datetime.utcnow)
class Wallet(Base):
 __tablename__="wallets";id=Column(Integer,primary_key=True);client_id=Column(Integer,unique=True,nullable=False);currency=Column(String(3),default="PEN");balance=Column(Float,default=0);status=Column(String(20),default="ACTIVE");created_at=Column(DateTime,default=datetime.utcnow)
class Tx(Base):
 __tablename__="wallet_transactions";id=Column(Integer,primary_key=True);wallet_id=Column(Integer,nullable=False);type=Column(String(40),nullable=False);amount=Column(Float,nullable=False);balance_after=Column(Float,nullable=False);currency=Column(String(3),default="PEN");reference=Column(String(120),unique=True,nullable=False);counterparty_wallet_id=Column(Integer);description=Column(String(255),default="");status=Column(String(20),default="COMPLETED");created_at=Column(DateTime,default=datetime.utcnow)
class Investment(Base):
 __tablename__="investments";id=Column(Integer,primary_key=True);client_id=Column(Integer,nullable=False);principal=Column(Float,nullable=False);currency=Column(String(3),nullable=False);annual_rate=Column(Float,nullable=False);term_days=Column(Integer,nullable=False);start_date=Column(DateTime,default=datetime.utcnow);maturity_date=Column(DateTime);status=Column(String(30),default="ACTIVE")
class Credit(Base):
 __tablename__="credits";id=Column(Integer,primary_key=True);client_id=Column(Integer,nullable=False);amount=Column(Float,nullable=False);currency=Column(String(3),default="PEN");annual_rate=Column(Float,nullable=False);term_months=Column(Integer,nullable=False);status=Column(String(30),default="OFFERED");campaign=Column(String(120),default="");created_at=Column(DateTime,default=datetime.utcnow)
class Campaign(Base):
 __tablename__="campaigns";id=Column(Integer,primary_key=True);name=Column(String(160),nullable=False);description=Column(Text,default="");min_amount=Column(Float,default=0);max_amount=Column(Float,default=0);annual_rate=Column(Float,default=0);term_months=Column(Integer,default=12);active=Column(Integer,default=1);created_at=Column(DateTime,default=datetime.utcnow)
Base.metadata.create_all(engine)
app=FastAPI(title="IZIDA - Billetera, Créditos e Inversiones",version="1.0")
app.add_middleware(CORSMiddleware,allow_origins=["*"],allow_credentials=True,allow_methods=["*"],allow_headers=["*"])
def db():
 s=SessionLocal()
 try: yield s
 finally:s.close()
def ref(p): return f"{p}-{datetime.utcnow().strftime('%Y%m%d%H%M%S')}-{secrets.token_hex(3).upper()}"
def wallet(s,i):
 w=s.get(Wallet,i)
 if not w or w.status!="ACTIVE": raise HTTPException(404,"Billetera no encontrada")
 return w
class ClientIn(BaseModel): document:str;name:str;phone:str="";email:str="";username:str;pin:str=""
class Amount(BaseModel): amount:float=Field(gt=0);description:str=""
class Transfer(BaseModel): destination_wallet_id:int;amount:float=Field(gt=0);description:str=""
class InvestmentIn(BaseModel): client_id:int;principal:float=Field(gt=0);currency:str="PEN";annual_rate:float=Field(gt=0);term_days:int=Field(gt=0)
class CreditIn(BaseModel): client_id:int;amount:float=Field(gt=0);annual_rate:float=0;term_months:int=Field(gt=0);campaign:str=""
class CampaignIn(BaseModel): name:str;description:str="";min_amount:float=0;max_amount:float=0;annual_rate:float=0;term_months:int=12
class Login(BaseModel): username:str;pin:str
@app.get("/api/health")
def health(): return {"ok":True,"brand":"IZIDA","wallet":"PEN","credits":"PEN","investments":["PEN","USD"]}
@app.post("/api/clients")
def clients_new(x:ClientIn,s:Session=Depends(db)):
 if s.query(Client).filter((Client.document==x.document)|(Client.username==x.username)).first(): raise HTTPException(409,"Documento o usuario ya existe")
 c=Client(document=x.document,name=x.name,phone=x.phone,email=x.email,username=x.username,pin_hash=x.pin);s.add(c);s.commit();s.refresh(c);s.add(Wallet(client_id=c.id));s.commit();return c
@app.get("/api/clients")
def clients(s:Session=Depends(db)): return s.query(Client).order_by(Client.id.desc()).all()
@app.post("/api/client/login")
def login(x:Login,s:Session=Depends(db)):
 c=s.query(Client).filter_by(username=x.username,status="ACTIVE").first()
 if not c or (c.pin_hash and c.pin_hash!=x.pin): raise HTTPException(401,"Usuario o PIN incorrecto")
 return {"authenticated":True,"client_id":c.id,"name":c.name}
@app.get("/api/client/{cid}/home")
def home(cid:int,s:Session=Depends(db)):
 c=s.get(Client,cid);w=s.query(Wallet).filter_by(client_id=cid).first()
 if not c or not w: raise HTTPException(404,"Cliente no encontrado")
 return {"client":c,"wallet":w,"transactions":s.query(Tx).filter_by(wallet_id=w.id).order_by(Tx.id.desc()).limit(30).all(),"credits":s.query(Credit).filter_by(client_id=cid).all(),"investments":s.query(Investment).filter_by(client_id=cid).all(),"campaigns":s.query(Campaign).filter_by(active=1).all()}
@app.get("/api/wallets")
def wallets(s:Session=Depends(db)): return s.query(Wallet).order_by(Wallet.id.desc()).all()
@app.post("/api/wallets/{wid}/deposit")
def deposit(wid:int,x:Amount,s:Session=Depends(db)):
 w=wallet(s,wid);w.balance+=x.amount;t=Tx(wallet_id=wid,type="DEPOSIT",amount=x.amount,balance_after=w.balance,currency="PEN",reference=ref("DEP"),description=x.description or "Ingreso IZIDA");s.add(t);s.commit();return t
@app.post("/api/wallets/{wid}/withdraw")
def withdraw(wid:int,x:Amount,s:Session=Depends(db)):
 w=wallet(s,wid)
 if w.balance<x.amount: raise HTTPException(400,"Saldo insuficiente")
 w.balance-=x.amount;t=Tx(wallet_id=wid,type="WITHDRAWAL",amount=x.amount,balance_after=w.balance,currency="PEN",reference=ref("WDR"),description=x.description or "Retiro IZIDA");s.add(t);s.commit();return t
@app.post("/api/wallets/{wid}/transfer")
def transfer(wid:int,x:Transfer,s:Session=Depends(db)):
 a=wallet(s,wid);b=wallet(s,x.destination_wallet_id)
 if a.id==b.id or a.balance<x.amount: raise HTTPException(400,"Transferencia inválida o saldo insuficiente")
 a.balance-=x.amount;b.balance+=x.amount;r=ref("TRF")
 s.add_all([Tx(wallet_id=a.id,type="TRANSFER_OUT",amount=x.amount,balance_after=a.balance,currency="PEN",reference=r+"O",counterparty_wallet_id=b.id,description=x.description or "Transferencia"),Tx(wallet_id=b.id,type="TRANSFER_IN",amount=x.amount,balance_after=b.balance,currency="PEN",reference=r+"I",counterparty_wallet_id=a.id,description=x.description or "Transferencia")]);s.commit();return {"ok":True,"reference":r}
@app.post("/api/investments")
def investment(x:InvestmentIn,s:Session=Depends(db)):
 if x.currency.upper() not in ("PEN","USD"): raise HTTPException(400,"Solo PEN o USD")
 now=datetime.utcnow();i=Investment(client_id=x.client_id,principal=x.principal,currency=x.currency.upper(),annual_rate=x.annual_rate,term_days=x.term_days,start_date=now,maturity_date=now+timedelta(days=x.term_days));s.add(i);s.commit();return i
@app.get("/api/investments")
def investments(s:Session=Depends(db)): return s.query(Investment).order_by(Investment.id.desc()).all()
@app.post("/api/admin/credits")
def credit(x:CreditIn,s:Session=Depends(db)):
 c=Credit(client_id=x.client_id,amount=x.amount,currency="PEN",annual_rate=x.annual_rate,term_months=x.term_months,campaign=x.campaign);s.add(c);s.commit();return c
@app.get("/api/admin/credits")
def credits(s:Session=Depends(db)): return s.query(Credit).order_by(Credit.id.desc()).all()
@app.post("/api/admin/credits/{cid}/approve")
def approve(cid:int,s:Session=Depends(db)):
 c=s.get(Credit,cid);w=s.query(Wallet).filter_by(client_id=c.client_id).first()
 if not c or not w: raise HTTPException(404,"Crédito o billetera no encontrados")
 if c.status!="APPROVED":
  c.status="APPROVED";w.balance+=c.amount;s.add(Tx(wallet_id=w.id,type="CREDIT_DISBURSEMENT",amount=c.amount,balance_after=w.balance,currency="PEN",reference=ref("CRD"),description=f"Desembolso crédito #{c.id}"));s.commit()
 return c
@app.post("/api/admin/campaigns")
def campaign(x:CampaignIn,s:Session=Depends(db)):
 c=Campaign(**x.model_dump());s.add(c);s.commit();return c
@app.get("/api/admin/campaigns")
def campaigns(s:Session=Depends(db)): return s.query(Campaign).order_by(Campaign.id.desc()).all()
@app.get("/api/admin/dashboard")
def dashboard(s:Session=Depends(db)):
 ws=s.query(Wallet).all();cr=s.query(Credit).all();iv=s.query(Investment).all()
 return {"clients":s.query(Client).count(),"wallets":len(ws),"wallet_balance_pen":sum(w.balance for w in ws),"credits":len(cr),"approved_credits_pen":sum(c.amount for c in cr if c.status=="APPROVED"),"investments_pen":sum(i.principal for i in iv if i.currency=="PEN"),"investments_usd":sum(i.principal for i in iv if i.currency=="USD")}
@app.get("/api/admin/client/{cid}/risk-profile")
def risk(cid:int,s:Session=Depends(db)):
 c=s.get(Client,cid);w=s.query(Wallet).filter_by(client_id=cid).first()
 if not c or not w: raise HTTPException(404,"Cliente no encontrado")
 return {"client":c,"wallet_balance_pen":w.balance,"transactions":s.query(Tx).filter_by(wallet_id=w.id).count(),"credits":s.query(Credit).filter_by(client_id=cid).count(),"investments":s.query(Investment).filter_by(client_id=cid).count(),"external_reports":{"sbs":"NO_CONECTADO","sentinel":"NO_CONECTADO"}}
@app.get("/api/integrations/status")
def integrations(): return {"credicorp":bool(os.getenv("CREDICORP_BASE_URL")),"intercorp":bool(os.getenv("INTERCORP_BASE_URL")),"sbs":False,"sentinel":False}