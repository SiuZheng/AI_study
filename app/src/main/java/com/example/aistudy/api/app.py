import os
from fastapi import FastAPI
from dotenv import load_dotenv
import json
import requests
import io
from datetime import date
from pydantic import BaseModel
from fastapi import FastAPI, File, UploadFile, Form
load_dotenv()

API_CHAT_KEY = os.getenv("DIFY_API_CHAT")
CHAT_URL = os.getenv("DIFY_URL")+"/chat-messages"
WORK_URL = os.getenv("DIFY_URL")+"/workflows/run"
API_WORK_KEY = os.getenv("DIFY_API_WORK")
upload_url = "https://api.dify.ai/v1/files/upload"
headers_chat = {
        "Authorization": f"Bearer {API_CHAT_KEY}",
        'Content-Type': 'application/json',
}

app = FastAPI()

class ChatInput(BaseModel):
    user_message: str
    conversation_id: str = None

@app.post("/chat") 
def get_dify_response(body: ChatInput):
    data = {
                "inputs":{},
                "query": body.user_message,
                "user":"siu",
                "respond_mode":"blocking",
                "files":[],
            }
    if body.conversation_id:
        data["conversation_id"] = body.conversation_id
        
    response = requests.post(CHAT_URL, data=json.dumps(data), headers=headers_chat)
    data = {
        'conversation_id':response.json().get('conversation_id'),
        'answer':response.json().get('answer','')
    }
    print(data)
    return data

@app.post("/workflow") 
async def get_dify_response_work_flow( 
    step: str = Form(...),
    file: UploadFile = File(None),
    flashcard_type: str = Form(None),
    planner_prompt:str = Form(None)
    ):
    if step == "flashcard":
        headers = {
                "Authorization": f"Bearer {API_WORK_KEY}"
            }
        if file:
            file_bytes = await file.read()
            file_obj = io.BytesIO(file_bytes)
            files = {
                "file": (file.filename, file_obj, file.content_type)
            }
            data = {
                "user": "user",
                "type": file.content_type 
            }
            response = requests.post(upload_url, headers=headers, files=files,data = data)
 
            if response.status_code == 201:  # 201 means creation is successful
                print("File uploaded successfully")
                file_id =response.json().get("id")
                print(file_id)
                headers = {
                    "Authorization": f"Bearer {API_WORK_KEY}",
                    "Content-Type": "application/json"
                }
                data = {
                    "inputs":{
                        "step":step,
                        "study_material": {
                            "transfer_method": "local_file",
                            "upload_file_id": f"{file_id}",
                            "type": "document"
                        }
                    },
                    "response_mode": "blocking",
                    "user": "user",

                }
                response = requests.post(WORK_URL, headers=headers, json=data)
                if response.status_code == 200:
                    print("Workflow execution successful")
                    text_data = json.loads(response.json()['data']['outputs']['text'])
                    flashcards = text_data
                    return flashcards
                else:
                    print("Error Response:", response.text)
                    print(f"Workflow execution failed, status code: {response.status_code}")
                    return None
            else:
                print(f"File upload failed, status code: {response.status_code}")
                return None
        else:
            print(flashcard_type)
            
            data = {
                    "inputs":{
                        "step":step,
                        "flashcard_type":flashcard_type
                    },
                    "response_mode": "blocking",
                    "user": "user",
                }
            response = requests.post(WORK_URL, headers=headers, json=data)
            print(response.status_code)
            text_data = json.loads(response.json()['data']['outputs']['text'])
            flashcards = text_data
            return flashcards
        
    elif step == "study planner":
        if planner_prompt:
            today = date.today()
            planner_prompt = f"today date is {today} "+planner_prompt
            headers = {
                "Authorization": f"Bearer {API_WORK_KEY}"
            }
            data = {
                    "inputs":{
                        "step":step,
                        "planner_prompt":planner_prompt
                    },
                    "response_mode": "blocking",
                    "user": "user",

                }
            response = requests.post(WORK_URL, headers=headers, json=data)
            print(response)
            text_data = json.loads(response.json()['data']['outputs']['text'])
            study_plan = text_data
            return study_plan