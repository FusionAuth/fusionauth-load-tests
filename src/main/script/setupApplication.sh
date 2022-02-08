#!/bin/bash
# Create / Update the FusionAuthLoadTesting application

FA_HOSTNAME=https://local.fusionauth.io

curl -s -X POST -H "Content-Type: application/json" \
  -H "Authorization: bf69486b-4733-4470-a592-f1bfce7af580" \
  -H "X-FusionAuth-TenantId: efb21cfc-fa60-46f4-9598-889151e58517" \
  -H "Cache-Control: no-cache" \
  -d \
  '{
     "application": {
       "name": "FusionAuthLoadTesting",
       "oauthConfiguration": {
          "authorizedRedirectURLs": ["https://acme.com/redirect"],
          "clientSecret": "ZDA5Yzk0NTUtYzg4ZS00ZmNhLThmOGUtZDFkN2M3YTllNDY1",
          "enabledGrants": ["authorization_code"]
       },
       "roles": ["admin", "user"]
     }
  }'\
  $FA_HOSTNAME/api/application/11e7ea7b-784d-4687-bf2d-4f8ee479a4dd | json_pp

curl -s -X PUT -H "Content-Type: application/json" \
  -H "Authorization: bf69486b-4733-4470-a592-f1bfce7af580" \
  -H "X-FusionAuth-TenantId: efb21cfc-fa60-46f4-9598-889151e58517" \
  -H "Cache-Control: no-cache" \
  -d \
  '{
     "application": {
       "name": "FusionAuthLoadTesting",
       "oauthConfiguration": {
         "authorizedRedirectURLs": ["https://acme.com/redirect"],
         "clientSecret": "ZDA5Yzk0NTUtYzg4ZS00ZmNhLThmOGUtZDFkN2M3YTllNDY1",
         "enabledGrants": ["authorization_code"]
       },
       "roles": ["admin", "user"]
     }
  }'\
  $FA_HOSTNAME/api/application/11e7ea7b-784d-4687-bf2d-4f8ee479a4dd | json_pp
