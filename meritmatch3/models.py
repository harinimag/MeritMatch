from sqlalchemy import Boolean, Column, Integer, String
from database import Base
from sqlalchemy import create_engine, Column, Integer, String, ForeignKey
from sqlalchemy.orm import relationship, sessionmaker
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class User(Base):
    __tablename__ = 'users'
    id = Column(Integer,primary_key = True, index = True)
    username = Column(String(50),unique = True)
    password = Column(String(50))
    karma = Column(Integer,default = 100)
    #tasks = relationship("Task", back_populates="user")

class Task(Base):
    __tablename__ = 'tasks'
    id = Column(Integer, primary_key = True, index = True)
    taskname = Column(String(50))
    content = Column(String(100))
    kpoints = Column(Integer)
    username = Column(String(50))
    #user_id = Column(Integer, ForeignKey('users.id'))
    #user = relationship("User", back_populates="tasks")

class Point(Base):
    __tablename__ = 'transactions'
    id = Column(Integer, primary_key = True, index = True)
    pfrom =  Column(Integer)
    pto = Column(Integer)
    points = Column(Integer) 

class Reserved(Base):
    __tablename__ = 'reserved'
    id = Column(Integer, primary_key = True, index = True)
    taskby = Column(String(50))
    reservedby = Column(String(50))
    taskname = Column(String(50))
    points = Column(Integer)

class Approval(Base):
    __tablename__ = 'approvals'
    id = Column(Integer, primary_key = True, index = True)
    afrom = Column(String(50))
    ato = Column(String(50))
    taskname = Column(String(50))
    points = Column(Integer)