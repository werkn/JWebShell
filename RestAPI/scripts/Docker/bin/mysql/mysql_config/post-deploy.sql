#perform our db setup
CREATE TABLE `Clients` ( 
  `IP` VARCHAR(15) NOT NULL,
  `Port` INT NOT NULL,
  `AccessToken` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`IP`, `Port`, `AccessToken`)
) ENGINE=InnoDB;

#TOKEN is a MD5 hash used for access
#Dummy query for testing
#INSERT INTO Clients values("192.168.0.1", 2087, "A4A43B59A356D07EFFB2BB3AEADE0681");
#INSERT INTO Clients values("192.168.0.2", 2087, "A4A43B59A356D07EFFB2BB3AEADE0681");
#INSERT INTO Clients values("192.168.0.3", 2087, "A4A43B59A356D07EFFB2BB3AEADE0681");