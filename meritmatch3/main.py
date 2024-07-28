from fastapi import FastAPI, HTTPException, Depends, status
from pydantic import BaseModel
import models
from database import engine, SessionLocal
from sqlalchemy.orm import Session
from typing import Annotated
from typing import List
from typing import Dict

app = FastAPI()
models.Base.metadata.create_all(bind=engine)

class UserBase(BaseModel):
    username: str
    password: str
    karma: int

class TaskBase(BaseModel):
    taskname: str
    content: str
    kpoints: int
    username: str
    #user_id: int
    
class PointBase(BaseModel):
    pfrom: int
    pto: int
    points: int

class ReservedBase(BaseModel):
    taskby: str
    reservedby: str
    taskname: str
    points: int

class ApprovalBase(BaseModel):
    afrom: str
    ato: str
    taskname: str
    points: int

class PointsUpdate(BaseModel):
    amount: int

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally: 
        db.close()

@app.post("/post_users", status_code=status.HTTP_201_CREATED)
async def create_user(user: UserBase, db: Session = Depends(get_db)):
    db_user = models.User(**user.model_dump())
    db.add(db_user)
    db.commit()


@app.post("/post_tasks", status_code=status.HTTP_201_CREATED)
async def create_task(task: TaskBase, db: Session = Depends(get_db)):
    db_task = models.Task(**task.model_dump())
    db.add(db_task)
    db.commit()

@app.get("/users/{username}", status_code=status.HTTP_200_OK)
async def read_user(username: str, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.username == username).first()
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return {"password": user.password,"points":user.karma}



@app.get("/tasks/{username}", response_model=List[Dict[str, str]],status_code=status.HTTP_200_OK)
async def read_task(username: str, db: Session = Depends(get_db)):
    tasks = db.query(models.Task).filter(models.Task.username == username).all()
    if not tasks:
        raise HTTPException(status_code=404, detail="Task not found")
    taskdetails = [{"taskname": task.taskname,"content":task.content,"kpoints":str(task.kpoints),"id":str(task.id)} for task in tasks]
    return taskdetails


@app.get("/get_users", response_model=List[str], status_code=status.HTTP_200_OK)
async def read_users(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    users = db.query(models.User).offset(skip).limit(limit).all()
    user_list = [user.username for user in users]    
    return user_list

@app.post("/post_rtasks", status_code=status.HTTP_201_CREATED)
async def create_rtask(rtask: ReservedBase, db: Session = Depends(get_db)):
    db_rtask = models.Reserved(**rtask.model_dump())
    db.add(db_rtask)
    db.commit()

@app.get("/rtasks/{username}", response_model=List[Dict[str, str]],status_code=status.HTTP_200_OK)
async def read_rtask(username: str, db: Session = Depends(get_db)):
    rtasks = db.query(models.Reserved).filter(models.Reserved.reservedby == username).all()
    if not rtasks:
        raise HTTPException(status_code=404, detail="Task not found")
    rtaskdetails = [{"taskname": rtask.taskname,"taskby":rtask.taskby,"points":str(rtask.points),"id":str(rtask.id)} for rtask in rtasks]
    return rtaskdetails

@app.delete("/delete_tasks/{task_id}", status_code=204)
async def delete_task(task_id: int, db: Session = Depends(get_db)):
    task = db.query(models.Task).filter(models.Task.id == task_id).first()    
    if task is None:
        raise HTTPException(status_code=404, detail="Task not found")
    db.delete(task)
    db.commit()


@app.post("/approve", status_code=status.HTTP_201_CREATED)
async def create_approval(approval: ApprovalBase, db: Session = Depends(get_db)):
    db_approval = models.Approval(**approval.model_dump())
    db.add(db_approval)
    db.commit()

@app.delete("/delete_rtasks/{rtask_id}", status_code=204)
async def delete_rtask(rtask_id: int, db: Session = Depends(get_db)):
    rtask = db.query(models.Reserved).filter(models.Reserved.id == rtask_id).first()    
    if rtask is None:
        raise HTTPException(status_code=404, detail="Task not found")
    db.delete(rtask)
    db.commit()


@app.get("/approval/{ato}", response_model=List[Dict[str, str]],status_code=status.HTTP_200_OK)
async def read_approval(ato: str, db: Session = Depends(get_db)):
    approvals = db.query(models.Approval).filter(models.Approval.ato == ato).all()
    if not approvals:
        raise HTTPException(status_code=404, detail="Task not found")
    approvaldetails = [{"afrom": approval.afrom,"taskname":approval.taskname,"points":str(approval.points),"id":str(approval.id)} for approval in approvals]
    return approvaldetails

@app.delete("/delete_approval/{approval_id}", status_code=204)
async def delete_approval(approval_id: int, db: Session = Depends(get_db)):
    approval = db.query(models.Approval).filter(models.Approval.id == approval_id).first()    
    if approval is None:
        raise HTTPException(status_code=404, detail="Task not found")
    db.delete(approval)
    db.commit()


@app.put("/increasep_users/{username}/increase_points",status_code=status.HTTP_204_NO_CONTENT)
async def increase_points(username: str, points_update: PointsUpdate, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.username == username).first()
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")
    user.karma += points_update.amount
    db.commit()

@app.put("/decreasep_users/{username}/decrease_points",status_code=status.HTTP_204_NO_CONTENT)
async def decrease_points(username: str, points_update: PointsUpdate, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.username == username).first()
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")
    user.karma -= points_update.amount
    db.commit()