# MT-API

## Funcitonaliteit 
### GET /home
Geeft een HTML-pagina terug die gepersonaliseerd is per tenant.
### GET /api/info
Geeft JSON-object terug met daarin de versie van de applicatie instantie.
### POST /api/login
Indien de inloggegevens correct zijn wordt er een JSONWebToken geretourneerd. Deze token moet gebruikt worden voor de andere endpoints. Deze token maakt geen gebruik van sessies en kan door elke applicatie instantie gecontroleerd worden. 
### POST /api/register
Staat toe een nieuwe gebruiker aan te maken. Deze wordt automatisch gelinkt aan de tenant. 
### GET /api/wait
Dit is een endpoint die een zwaardere taak op de applicatie instantie simuleert.
### GET /admin 
Geeft een HTML-pagina terug die dient als administrator paneel.

### POST /admin/tenant
Endpoint om een nieuwe tenant aan te maken. 
### GET /admin/tenant?name=
Geeft de tenant die die naam heeft, terug. 
### POST /admin/tenant/version
Staat toe de versie waarop een tenant recht heeft te veranderen 
### GET /admin/tenants/version
Staat toe meerdere tenants tegelijkertijd van versie te veranderen.
