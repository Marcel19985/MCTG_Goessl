@echo off

REM --------------------------------------------------
REM Monster Trading Cards Game
REM --------------------------------------------------
title Monster Trading Cards Game
echo CURL Testing for Monster Trading Cards Game
echo Syntax: $1 [pause]
echo - pause: optional, if set, the script will pause after each block
echo.

set "pauseFlag=0"
for %%a in (%*) do (
    if /I "%%a"=="pause" (
        set "pauseFlag=1"
    )
)

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 1) Create Users (Registration)
REM Create User
curl -i -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"Username\":\"kienboec\", \"Password\":\"daniel\"}"
echo "Should return HTTP 201"
echo.
curl -i -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"Username\":\"altenhof\", \"Password\":\"markus\"}"
echo "Should return HTTP 201"
echo.
curl -i -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"Username\":\"admin\",    \"Password\":\"istrator\"}"
echo "Should return HTTP 201"
echo.

if %pauseFlag%==1 pause

echo should fail:
curl -i -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"Username\":\"kienboec\", \"Password\":\"daniel\"}"
echo "Should return HTTP 4xx - User already exists"
echo.
curl -i -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"Username\":\"kienboec\", \"Password\":\"different\"}"
echo "Should return HTTP 4xx - User already exists"
echo. 
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 2) Login Users
curl -i -X POST http://localhost:10001/sessions --header "Content-Type: application/json" -d "{\"Username\":\"kienboec\", \"Password\":\"daniel\"}"
echo "should return HTTP 200 with generated token for the user, here: kienboec-mtcgToken"
echo.
curl -i -X POST http://localhost:10001/sessions --header "Content-Type: application/json" -d "{\"Username\":\"altenhof\", \"Password\":\"markus\"}"
echo "should return HTTP 200 with generated token for the user, here: altenhof-mtcgToken"
echo.
curl -i -X POST http://localhost:10001/sessions --header "Content-Type: application/json" -d "{\"Username\":\"admin\",    \"Password\":\"istrator\"}"
echo "should return HTTP 200 with generated token for the user, here: admin-mtcgToken"
echo.

if %pauseFlag%==1 pause

echo should fail:
curl -i -X POST http://localhost:10001/sessions --header "Content-Type: application/json" -d "{\"Username\":\"kienboec\", \"Password\":\"different\"}"
echo "Should return HTTP 4xx - Login failed"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------

echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 4) acquire packages kienboec
curl -i -X POST http://localhost:10001/transactions/packages --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-mtcgToken" -d ""
echo "Should return HTTP 201"
echo.
curl -i -X POST http://localhost:10001/transactions/packages --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-mtcgToken" -d ""
echo "Should return HTTP 201"
echo.
curl -i -X POST http://localhost:10001/transactions/packages --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-mtcgToken" -d ""
echo "Should return HTTP 201"
echo.
curl -i -X POST http://localhost:10001/transactions/packages --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-mtcgToken" -d ""
echo "Should return HTTP 201"
echo.
echo should fail (no money):
curl -i -X POST http://localhost:10001/transactions/packages --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-mtcgToken" -d ""
echo "Should return HTTP 4xx - Not enough money"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 5) acquire packages altenhof
curl -i -X POST http://localhost:10001/transactions/packages --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-mtcgToken" -d ""
echo "Should return HTTP 201"
echo.
curl -i -X POST http://localhost:10001/transactions/packages --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-mtcgToken" -d ""
echo "Should return HTTP 201"
echo.
echo should fail (no package):
curl -i -X POST http://localhost:10001/transactions/packages --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-mtcgToken" -d ""
echo "Should return HTTP 4xx - No packages available"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 6) add new packages
curl -i -X POST http://localhost:10001/packages --header "Content-Type: application/json" --header "Authorization: Bearer admin-mtcgToken" -d "[{\"Id\":\"67f9048f-99b8-4ae4-b866-d8008d00c53d\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"aa9999a0-734c-49c6-8f4a-651864b14e62\", \"Name\":\"RegularSpell\", \"Damage\": 50.0}, {\"Id\":\"d6e9c720-9b5a-40c7-a6b2-bc34752e3463\", \"Name\":\"Knight\", \"Damage\": 20.0}, {\"Id\":\"02a9c76e-b17d-427f-9240-2dd49b0d3bfd\", \"Name\":\"RegularSpell\", \"Damage\": 45.0}, {\"Id\":\"2508bf5c-20d7-43b4-8c77-bc677decadef\", \"Name\":\"FireElf\", \"Damage\": 25.0}]"
echo "Should return HTTP 201"
echo.
curl -i -X POST http://localhost:10001/packages --header "Content-Type: application/json" --header "Authorization: Bearer admin-mtcgToken" -d "[{\"Id\":\"70962948-2bf7-44a9-9ded-8c68eeac7793\", \"Name\":\"WaterGoblin\", \"Damage\":  9.0}, {\"Id\":\"74635fae-8ad3-4295-9139-320ab89c2844\", \"Name\":\"FireSpell\", \"Damage\": 55.0}, {\"Id\":\"ce6bcaee-47e1-4011-a49e-5a4d7d4245f3\", \"Name\":\"Knight\", \"Damage\": 21.0}, {\"Id\":\"a6fde738-c65a-4b10-b400-6fef0fdb28ba\", \"Name\":\"FireSpell\", \"Damage\": 55.0}, {\"Id\":\"a1618f1e-4f4c-4e09-9647-87e16f1edd2d\", \"Name\":\"FireElf\", \"Damage\": 23.0}]"
echo "Should return HTTP 201"
echo.
curl -i -X POST http://localhost:10001/packages --header "Content-Type: application/json" --header "Authorization: Bearer admin-mtcgToken" -d "[{\"Id\":\"2272ba48-6662-404d-a9a1-41a9bed316d9\", \"Name\":\"WaterGoblin\", \"Damage\": 11.0}, {\"Id\":\"3871d45b-b630-4a0d-8bc6-a5fc56b6a043\", \"Name\":\"Dragon\", \"Damage\": 70.0}, {\"Id\":\"166c1fd5-4dcb-41a8-91cb-f45dcd57cef3\", \"Name\":\"Knight\", \"Damage\": 22.0}, {\"Id\":\"237dbaef-49e3-4c23-b64b-abf5c087b276\", \"Name\":\"WaterSpell\", \"Damage\": 40.0}, {\"Id\":\"27051a20-8580-43ff-a473-e986b52f297a\", \"Name\":\"FireElf\", \"Damage\": 28.0}]"
echo "Should return HTTP 201"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 7) acquire newly created packages altenhof
curl -i -X POST http://localhost:10001/transactions/packages --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-mtcgToken" -d ""
echo "Should return HTTP 201"
echo.
curl -i -X POST http://localhost:10001/transactions/packages --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-mtcgToken" -d ""
echo "Should return HTTP 201"
echo.
echo should fail (no money):
curl -i -X POST http://localhost:10001/transactions/packages --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-mtcgToken" -d ""
echo "Should return HTTP 4xx - Not enough money"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 8) show all acquired cards kienboec
curl -i -X GET http://localhost:10001/cards --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and a list of all cards"
echo should fail (no token)
curl -i -X GET http://localhost:10001/cards 
echo "Should return HTTP 4xx - Unauthorized"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 9) show all acquired cards altenhof
curl -i -X GET http://localhost:10001/cards --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and a list of all cards"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 10) show unconfigured deck
curl -i -X GET http://localhost:10001/deck --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and a empty-list"
echo.
curl -i -X GET http://localhost:10001/deck --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and a empty-list"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 11) configure deck
curl -i -X PUT http://localhost:10001/deck --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-mtcgToken" -d "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]"
echo "Should return HTTP 2xx"
echo.
curl -i -X GET http://localhost:10001/deck --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and a list of all cards"
echo.
curl -i -X PUT http://localhost:10001/deck --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-mtcgToken" -d "[\"aa9999a0-734c-49c6-8f4a-651864b14e62\", \"d6e9c720-9b5a-40c7-a6b2-bc34752e3463\", \"d60e23cf-2238-4d49-844f-c7589ee5342e\", \"02a9c76e-b17d-427f-9240-2dd49b0d3bfd\"]"
echo "Should return HTTP 2xx"
echo.
curl -i -X GET http://localhost:10001/deck --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and a list of all cards"
echo.
echo.

if %pauseFlag%==1 pause
echo should fail and show original from before:
curl -i -X PUT http://localhost:10001/deck --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-mtcgToken" -d "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]"
echo "Should return HTTP 4xx"
echo.
curl -i -X GET http://localhost:10001/deck --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and a list of all cards"
echo.
echo.
echo should fail ... only 3 cards set
curl -i -X PUT http://localhost:10001/deck --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-mtcgToken" -d "[\"aa9999a0-734c-49c6-8f4a-651864b14e62\", \"d6e9c720-9b5a-40c7-a6b2-bc34752e3463\", \"d60e23cf-2238-4d49-844f-c7589ee5342e\"]"
echo "Should return HTTP 4xx - Bad request"
echo.

if %pauseFlag%==1 pause


REM --------------------------------------------------
echo 12) show configured deck 
curl -i -X GET http://localhost:10001/deck --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and a list of all cards"
echo.
curl -i -X GET http://localhost:10001/deck --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and a list of all cards"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 13) show configured deck different representation
echo kienboec
curl -i -X GET "http://localhost:10001/deck?format=plain" --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and a list of all cards"
echo.
echo.
echo altenhof
curl -i -X GET "http://localhost:10001/deck?format=plain" --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and a list of all cards"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 14) edit user data
echo.
curl -i -X GET http://localhost:10001/users/kienboec --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and current user data"
echo.
curl -i -X GET http://localhost:10001/users/altenhof --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and current user data"
echo.
curl -i -X PUT http://localhost:10001/users/kienboec --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-mtcgToken" -d "{\"Name\": \"Kienboeck\",  \"Bio\": \"me playin...\", \"Image\": \":-)\"}"
echo "Should return HTTP 2xx"
echo.
curl -i -X PUT http://localhost:10001/users/altenhof --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-mtcgToken" -d "{\"Name\": \"Altenhofer\", \"Bio\": \"me codin...\",  \"Image\": \":-D\"}"
echo "Should return HTTP 2xx"
echo.
curl -i -X GET http://localhost:10001/users/kienboec --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and new user data"
echo.
curl -i -X GET http://localhost:10001/users/altenhof --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and new user data"
echo.
echo.

if %pauseFlag%==1 pause

echo should fail:
curl -i -X GET http://localhost:10001/users/altenhof --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 4xx"
echo.
curl -i -X GET http://localhost:10001/users/kienboec --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 4xx"
echo.
curl -i -X PUT http://localhost:10001/users/kienboec --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-mtcgToken" -d "{\"Name\": \"Hoax\",  \"Bio\": \"me playin...\", \"Image\": \":-)\"}"
echo "Should return HTTP 4xx"
echo.
curl -i -X PUT http://localhost:10001/users/altenhof --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-mtcgToken" -d "{\"Name\": \"Hoax\", \"Bio\": \"me codin...\",  \"Image\": \":-D\"}"
echo "Should return HTTP 4xx"
echo.
curl -i -X GET http://localhost:10001/users/someGuy  --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 4xx"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 15) stats
curl -i -X GET http://localhost:10001/stats --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and user stats"
echo.
curl -i -X GET http://localhost:10001/stats --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and user stats"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 16) scoreboard
curl -i -X GET http://localhost:10001/scoreboard --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and the scoreboard"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 17) battle
start /b "kienboec battle" curl -i -X POST http://localhost:10001/battles --header "Authorization: Bearer kienboec-mtcgToken"
start /b "altenhof battle" curl -i -X POST http://localhost:10001/battles --header "Authorization: Bearer altenhof-mtcgToken"
ping localhost -n 10 >NUL 2>NUL

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 18) Stats 
echo kienboec
curl -i -X GET http://localhost:10001/stats --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and changed user stats"
echo.
echo altenhof
curl -i -X GET http://localhost:10001/stats --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and changed user stats"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 19) scoreboard
curl -i -X GET http://localhost:10001/scoreboard --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and the changed scoreboard"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 20) trade
echo check trading deals
curl -i -X GET http://localhost:10001/tradings --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and an empty list"
echo.
echo create trading deal
curl -i -X POST http://localhost:10001/tradings --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-mtcgToken" -d "{\"Id\": \"6cd85277-4590-49d4-b0cf-ba0a921faad0\", \"CardToTrade\": \"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Type\": \"monster\", \"MinimumDamage\": 15}"
echo "Should return HTTP 201"
echo.

if %pauseFlag%==1 pause

echo check trading deals
curl -i -X GET http://localhost:10001/tradings --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 - and the trading deal"
echo.
curl -i -X GET http://localhost:10001/tradings --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 - and the trading deal"
echo.

if %pauseFlag%==1 pause

echo delete trading deals
curl -i -X DELETE http://localhost:10001/tradings/6cd85277-4590-49d4-b0cf-ba0a921faad0 --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 2xx"
echo.
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo 21) check trading deals
curl -i -X GET http://localhost:10001/tradings  --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 ..."
echo.
curl -i -X POST http://localhost:10001/tradings --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-mtcgToken" -d "{\"Id\": \"6cd85277-4590-49d4-b0cf-ba0a921faad0\", \"CardToTrade\": \"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Type\": \"monster\", \"MinimumDamage\": 15}"
echo "Should return HTTP 201"
echo check trading deals
curl -i -X GET http://localhost:10001/tradings  --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 ..."
echo.
curl -i -X GET http://localhost:10001/tradings  --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 ..."
echo.

if %pauseFlag%==1 pause

echo try to trade with yourself (should fail)
curl -i -X POST http://localhost:10001/tradings/6cd85277-4590-49d4-b0cf-ba0a921faad0 --header "Content-Type: application/json" --header "Authorization: Bearer kienboec-mtcgToken" -d "\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\""
echo "Should return HTTP 4xx"
echo.

if %pauseFlag%==1 pause

echo try to trade 
echo.
curl -i -X POST http://localhost:10001/tradings/6cd85277-4590-49d4-b0cf-ba0a921faad0 --header "Content-Type: application/json" --header "Authorization: Bearer altenhof-mtcgToken" -d "\"951e886a-0fbf-425d-8df5-af2ee4830d85\""
echo "Should return HTTP 201 ..."
echo.
curl -i -X GET http://localhost:10001/tradings --header "Authorization: Bearer kienboec-mtcgToken"
echo "Should return HTTP 200 ..."
echo.
curl -i -X GET http://localhost:10001/tradings --header "Authorization: Bearer altenhof-mtcgToken"
echo "Should return HTTP 200 ..."
echo.

if %pauseFlag%==1 pause

REM --------------------------------------------------
echo end...

REM this is approx a sleep 
ping localhost -n 100 >NUL 2>NUL
@echo on
