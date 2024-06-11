

##############################################################################

# Para el back-end

drop table if exists reles;
drop table if exists sLuz;
drop table if exists sTemp;

# Creacion de tablas 

create table reles(
id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
idRele INT NOT NULL,
estado BOOL NOT NULL,
tStamp BIGINT NOT NULL,
idPlaca INT NOT NULL,
idGroup INT NOT NULL,
tipo VARCHAR(20) NOT NULL
);

create table sLuz(
id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
idSLuz INT NOT NULL,
valor DOUBLE NOT NULL,
tStamp BIGINT NOT NULL,
idPlaca INT NOT NULL,
idGroup INT NOT NULL
);

create table sTemp(
id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
idSTemp INT NOT NULL,
valor DOUBLE NOT NULL,
tStamp BIGINT NOT NULL,
idPlaca INT NOT NULL,
idGroup INT NOT NULL
);




##############################################################################

# Para el front-end

drop table if exists usuarios;
drop table if exists grupoYUsuarios;

create table usuarios(
id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(32) UNIQUE NOT NULL,
password VARCHAR(256) NOT NULL
);



create table grupoYUsuarios(
id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(32) NOT NULL,
idGroup INT UNIQUE NOT NULL, # Un usuario puede tener varios grupos, un grupo no puede ser de varios usuarios
manualOverride BOOLEAN NOT NULL
);

##############################################################################










# Pruebas varias:

INSERT INTO reles (idRele, estado, tstamp, idPlaca, idGroup, tipo) VALUES (1,false,1,0,3,"Bombilla");
INSERT INTO reles (idRele, estado, tstamp, idPlaca, idGroup, tipo) VALUES (1,true,4,0,3,"Bombilla");
INSERT INTO reles (idRele, estado, tstamp, idPlaca, idGroup, tipo) VALUES (5,false,3,1,35,"Ventilador");


INSERT INTO sTemp (idSTemp, valor, tstamp, idPlaca, idGroup) VALUES (9,45.2,3,1,35);
INSERT INTO sTemp (idSTemp, valor, tstamp, idPlaca, idGroup) VALUES (9,45.2,4,1,35);
INSERT INTO sTemp (idSTemp, valor, tstamp, idPlaca, idGroup) VALUES (9,45.2,5,1,35);


SELECT version()

SELECT * FROM domoticadad.reles WHERE idRele = 4 ORDER BY tStamp DESC LIMIT 1




SELECT r1.*
FROM reles r1
INNER JOIN (
    SELECT idRele, MAX(tStamp) AS maxTimestamp
    FROM reles
    WHERE idGroup = ?
    GROUP BY idRele
) r2 ON r1.idRele = r2.idRele AND r1.tStamp = r2.maxTimestamp
WHERE r1.idGroup = ?;





SELECT r1.* FROM domoticadad.reles r1  INNER JOIN ( SELECT idRele, idPlaca, MAX(tStamp) AS maxTimestamp FROM domoticadad.reles WHERE idGroup = 35 GROUP BY idRele, idPlaca ) r2 ON r1.idRele = r2.idRele AND r1.tStamp = r2.maxTimestamp WHERE r1.idGroup = 35


INSERT INTO grupoYUsuarios (username, idGroup) VALUES ("Pablo", 34);
