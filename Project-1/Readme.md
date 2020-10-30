# WeatNet

Tasks

- [ ] Client creation. The application should be designed with client scalability in mind even though the testing would be done with max 5 clients.
- [ ] Single client connection with the server
- [ ] Multiple clients simultaneously connecting with the server
- [ ] Authentication procedure for a single randomly chosen client
- [ ] The process from initiating a request by the client side to the final verification of
the received file at the client side and file display.
- [ ] Process in case of a mismatch between received file and received hash.


## Authentication Protocol

Auth_Request 0  Payload
Auth_Challenge 1    Question (String)    
Auth_Fail 2     Reason of failure (String)
Auth_Success 3      Token (String)

| Message type   |      Value      |  Payload |
|----------|:-------------:|------:|
| Auth_Request |  0 | Username/Answer (String |
| Auth_Challenge |    1   |   Question (String) |
| Auth_Fail | 2 |    Reason of failure (String) |
| Auth_Success | 3 |    Token (String) |
 
## Server-Client Protocol

| Message type   |      Value      |  Payload |
|----------|:-------------:|------:|
| Api_Request |  0 |  + Token (String) |
| Api_Response |  1 |  |
| Api_Response_Data |  2 |  |
| Api_Response_Hash |  3 |  |

## OWM API metrics:
1. Current weather forecast
2. Weather triggers
3. Basic weather maps
4. Minute forecast for 1 hour
6. Historical weather for 5 days