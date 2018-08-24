#!/bin/bash
# Create / Update the FusionAuthLoadTesting application

curl -s -X POST -H "Content-Type: application/json" \
  -H "Authorization: bf69486b-4733-4470-a592-f1bfce7af580" \
  -H "Cache-Control: no-cache" \
  -d \
  '{
     "application": {
       "name": "FusionAuthLoadTesting",
       "roles": ["admin", "user"]
     }
  }'\
  http://localhost:9011/api/application/11e7ea7b-784d-4687-bf2d-4f8ee479a4dd | json_pp

curl -s -X PUT -H "Content-Type: application/json" \
  -H "Authorization: bf69486b-4733-4470-a592-f1bfce7af580" \
  -H "Cache-Control: no-cache" \
  -d \
  '{
     "application": {
       "name": "FusionAuthLoadTesting",
       "roles": ["admin", "user"]
     }
  }'\
  http://localhost:9011/api/application/11e7ea7b-784d-4687-bf2d-4f8ee479a4dd | json_pp