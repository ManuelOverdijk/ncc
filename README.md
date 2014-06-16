**Plan van aanpak NCC**
===================

**Idee**
In project drone gaan we proberen een quadcopter aan te sturen die verschillende acties zou kunnen ondernemen. 
Hier gaat het vooral om de functionaliteit van het geheel die zich indien mogelijk vertaalt in praktische toepassingen. 
We hebben een quadcopter gekocht die voldoet aan onze eisen. Hier hebben we hard over na moeten denken maar we zijn uiteindelijk 
uitgekomen op de crazyflie 10-DOF kit. Het oorspronkelijke plan was de quadcopter samen te stellen uit verschillende losse 
onderdelen en het vervolgens zelf te assembleren. Dit bleek echter niet mogelijk te zijn omdat de lange levertijden niet pasten 
in het werkplan van het project aangezien meeste onderdelen waarschijnlijk pas in de laatste week binnen zouden komen. De voordelen 
van de crazyflie waren aanzienlijk, aangezien we de kit binnen de gewenste tijd binnen hadden gekregen konden we alvast de quadcoper 
in elkaar solderen en hebben we genoeg tijd om te programmeren. Dit laatste hebben we nodig aangezien we van plan zijn communicatie 
tussen meerdere android apparaten tussen de computer en de crazyflie willen realiseren. Dit kunnen we eventueel uitbreiden door 
in plaats van een computer een raspberry pi te gebruiken. Het uiteindelijke plan is een gewogen graaf tussen de master(pi of computer) 
en de slaves(androids) te construeren. De master laat dan de drone via het “kortste pad” naar de slaven vliegen. Dit kan als de 
crazyflie wordt uitgerust met GPS en alle master en slaves allemaal onderling met elkaar verbonden zijn. Om dit voor elkaar te krijgen 
is het plan om een uBLOX MAX-7 Pico Breakout with Chip Scale Antenna boven op de crazyflie te monteren.

Taken per apparaat
===============
**Master:**
- Bij (dis)connect slave: maak nieuwe random graaf. 
	- Via GUI kan je bestemming drone kiezen.
	- Bepaalt wanneer shortest paths opnieuw berekend moeten worden (om de x seconden  
en bij (dis)connecten slave)
- Heeft aparte thread of service die constant de gps-positie van de drone binnenkrijgt en 
hiermee de richting van de drone bepaalt.

**Slave:**
- geeft GPS locatie als master hierom vraagt. 
- berekent samen met andere slaves shortest path

**Drone:**
- verstuurt locatie naar master (push of pull)
- krijgt vlieg-instructies van master

**Notes**
===========
Open source code voor vliegen naar gps locatie:
https://github.com/diydrones/ardupilot?files=1

Master kan raspberry pi, PC of Android zijn. Communicatie android-pi is wss lastiger dan android-android, maar met de PC client kan 
je de drone wel veel beter monitoren (is ook een eis voor het project)



sssp algoritme:
http://dl.acm.org/citation.cfm?id=358690.358717&coll=DL&dl=ACM&CFID=356137276&CFTOKEN=72989188
Er bestaan meerdere algoritmen


Toevoegen van GPS module
http://wiki.bitcraze.se/projects:crazyflie:hacks:gps

Deze software om gps te gebruiken
http://marble.kde.org
