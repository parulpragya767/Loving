import json
import os
from typing import Dict, List, Optional, Any
from dataclasses import dataclass

# API Configuration
OPENAI_API_BASE_URL = "https://api.openai.com/v1/chat/completions"
OPENAI_API_MODEL = "gpt-4"

POPULATE_RITUAL_DETAILS_PROMPT_FILE = "../prompts/populate_ritual_details_prompt.txt"

@dataclass
class RitualDetails:
    """Data class representing structured ritual details from LLM"""
    tagline: str
    description: str
    steps: List[str]
    howItHelps: str
    loveType: List[str]
    ritualMode: str
    timeTaken: str
    relationalNeed: List[str]
    tone: str


class RitualLLMPopulator:
    """
    Utility class for populating ritual details using LLM with structured JSON output.
    """
    
    def __init__(self):
        self.api_key = os.environ['OPENAI_API_KEY']
        self.model = OPENAI_API_MODEL
        self.base_url = OPENAI_API_BASE_URL
    
    def _load_prompt_template(self) -> str:
        """Load the prompt template for ritual generation from file."""
        try:
            # Get the directory of the current script
            script_dir = os.path.dirname(os.path.abspath(__file__))
            prompt_file_path = os.path.join(script_dir, POPULATE_RITUAL_DETAILS_PROMPT_FILE)
            
            with open(prompt_file_path, 'r', encoding='utf-8') as file:
                return file.read()
        except FileNotFoundError:
            raise FileNotFoundError(f"Prompt file not found at: {prompt_file_path}")
        except Exception as e:
            raise Exception(f"Error loading prompt template: {str(e)}")
    
    def _call_llm(self, prompt: str) -> Dict[str, Any]:
        """
        Make API call to OpenAI with structured JSON output.
        """
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        
        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": "You are a ritual designer for the Loving App. Always respond with valid JSON only."},
                {"role": "user", "content": prompt}
            ],
            "temperature": 0.7,
            "response_format": {"type": "json_object"}
        }
        
        try:
            response = requests.post(self.base_url, headers=headers, json=payload, timeout=30)
            response.raise_for_status()
            
            result = response.json()
            content = result['choices'][0]['message']['content']
            
            # Parse the JSON response
            return json.loads(content)
            
        except requests.exceptions.RequestException as e:
            raise Exception(f"API request failed: {str(e)}")
        except json.JSONDecodeError as e:
            raise Exception(f"Failed to parse JSON response: {str(e)}")
        except KeyError as e:
            raise Exception(f"Unexpected response format: {str(e)}")
    
    def generate_ritual_details(self, title: str, prompt: str) -> RitualDetails:
        """
        Generate complete ritual details using LLM.
        """
        # Format the prompt template
        formatted_prompt = self._load_prompt_template().format(
            title=title,
            prompt=prompt
        )
        
        # Call LLM
        response = self._call_llm(formatted_prompt)
        
        # Convert to RitualDetails object
        return RitualDetails(
            tagline=response.get('tagline', ''),
            description=response.get('description', ''),
            steps=response.get('steps', []),
            howItHelps=response.get('howItHelps', ''),
            loveType=response.get('loveType', []),
            ritualMode=response.get('ritualMode', ''),
            timeTaken=response.get('timeTaken', ''),
            relationalNeed=response.get('relationalNeed', []),
            tone=response.get('tone', '')
        )
    
    def populate_missing_ritual_fields_batch(self, batch: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        Populate missing ritual fields for a batch of rituals using LLM.
        """
        print(f"  > Populating ritual details for {len(batch)} rituals using LLM...")
        
        for i, ritual in enumerate(batch):
            try:
                title = ritual.get("Title", "")
                prompt = ritual.get("Prompt", ritual.get("Description", ""))
                
                if not title or not prompt:
                    print(f"  > Warning: Skipping ritual {i+1} due to missing title or prompt")
                    continue
                
                # Generate ritual details using LLM
                details = self.generate_ritual_details(title, prompt)
                
                # Update ritual with generated details
                ritual["Tagline"] = details.tagline
                ritual["Description"] = details.description
                ritual["Steps"] = "\n".join(details.steps)
                ritual["How It Helps"] = details.howItHelps
                ritual["Love Type"] = ", ".join(details.loveType)
                ritual["Ritual Mode"] = details.ritualMode
                ritual["Time Taken"] = details.timeTaken
                ritual["Relational Need"] = ", ".join(details.relationalNeed)
                ritual["Tone"] = details.tone
                ritual["Sync Status"] = 'REVIEW'
                
                print(f"  > Successfully populated ritual {i+1}: {title}")
                
            except Exception as e:
                print(f"  > Error populating ritual {i+1}: {str(e)}")
                # Set status to indicate failure
                ritual["Sync Status"] = 'ERROR'
                continue
        
        return batch
