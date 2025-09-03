-- V3__seed_test_users.sql
-- Este script puebla la base de datos con 33 usuarios de prueba usando datos generados por Faker.
-- ¡¡¡NO EJECUTAR EN PRODUCCIÓN!!!

-- Se han generado passwords aleatorios. Todos tienen la misma contraseña: "password"
-- El password hasheado con BCrypt ($2a$10$...) corresponde a "password".
SET @password_hash = '$2a$10$e.w2.b6x9g3lY0y.U9wZcO.0gH6v/Wd5c7J.fK6uXkL3e/mY0b6S';

INSERT INTO `user` (id, username, password, email, full_name, enabled) VALUES
(101, 'eva.pastor', @password_hash, 'alejandra55@example.org', 'Sra. Eva Pastor', true),
(102, 'elena51', @password_hash, 'valeria.mena@example.org', 'Elena Rivas', true),
(103, 'alex.sala', @password_hash, 'rodriguez.manuela@example.com', 'Sr. Alejandro Sala', true),
(104, 'guillermo.mendez', @password_hash, 'isabel13@example.org', 'Guillermo Méndez Soler', true),
(105, 'cruz.lucas', @password_hash, 'sara.guerrero@example.net', 'Lucas Cruz', true),
(106, 'teresa.dominguez', @password_hash, 'josefa22@example.net', 'Teresa Domínguez', true),
(107, 'paula40', @password_hash, 'asuncion52@example.net', 'Paula Bravo Nieto', true),
(108, 'pilar.gallego', @password_hash, 'juan.diaz@example.com', 'Pilar Gallego Soler', true),
(109, 'esteban.carrasco', @password_hash, 'julia.iglesias@example.org', 'Esteban Carrasco-Serrano', true),
(110, 'lucas.caballero', @password_hash, 'tomas.rubio@example.org', 'Lucas Caballero', true),
(111, 'sara.leon', @password_hash, 'agustin.perez@example.net', 'Sara León-Iglesias', true),
(112, 'pablo77', @password_hash, 'manuel.gimenez@example.net', 'Pablo Navarro', true),
(113, 'cabrera.gloria', @password_hash, 'marta57@example.org', 'Gloria Cabrera', true),
(114, 'vazquez.gabriel', @password_hash, 'alejandro93@example.net', 'Gabriel Vázquez', true),
(115, 'rocio22', @password_hash, 'daniel68@example.org', 'Rocío Santos', true),
(116, 'garrido.pablo', @password_hash, 'maria59@example.com', 'Pablo Garrido-Sáez', true),
(117, 'ortega.alicia', @password_hash, 'elena63@example.net', 'Alicia Ortega', true),
(118, 'moya.alejandro', @password_hash, 'daniel.diaz@example.org', 'Alejandro Moya', true),
(119, 'santiago.mendez', @password_hash, 'cristobal46@example.org', 'Santiago Méndez', true),
(120, 'martin.ibañez', @password_hash, 'pascual.cruz@example.net', 'Martín Ibáñez', true),
(121, 'alicia.soler', @password_hash, 'blanca34@example.org', 'Alicia Soler', true),
(122, 'cristina71', @password_hash, 'alonso.andres@example.net', 'Cristina Moya', true),
(123, 'molina.maria', @password_hash, 'mario.alonso@example.com', 'María Molina', true),
(124, 'marquez.juan', @password_hash, 'carla.gallego@example.org', 'Juan Márquez', true),
(125, 'vega.isabel', @password_hash, 'david.ibañez@example.net', 'Isabel Vega', true),
(126, 'gonzalez.hugo', @password_hash, 'carlos87@example.org', 'Hugo González-Herrera', true),
(127, 'marina15', @password_hash, 'ortega.hector@example.net', 'Marina Campos', true),
(128, 'lucia15', @password_hash, 'vicente67@example.org', 'Lucía Alonso-Crespo', true),
(129, 'santos.teresa', @password_hash, 'mario.ruiz@example.org', 'Teresa Santos', true),
(130, 'david32', @password_hash, 'vicente02@example.net', 'David Pascual', true),
(131, 'gomez.jose', @password_hash, 'carmen.jimenez@example.net', 'Jose Gómez-Lozano', true),
(132, 'vicente23', @password_hash, 'lucia.santos@example.com', 'Vicente Navarro-León', true),
(133, 'alba.fuentes', @password_hash, 'pablo.fernandez@example.org', 'Alba Fuentes-Mora', true);


-- Asignación de Roles Aleatoria
-- Un usuario tendrá siempre rol 'VIEWER' (3), y aleatoriamente 'ADMIN' (1) o 'EDITOR' (2), o ambos.
INSERT INTO `user_role` (`user`, `role`) VALUES
(101, 3), (101, 2),
(102, 3), (102, 1),
(103, 3), (103, 1), (103, 2),
(104, 3),
(105, 3), (105, 1), (105, 2),
(106, 3),
(107, 3),
(108, 3),
(109, 3), (109, 2),
(110, 3), (110, 2),
(111, 3), (111, 1),
(112, 3), (112, 1),
(113, 3),
(114, 3),
(115, 3), (115, 2),
(116, 3), (116, 1), (116, 2),
(117, 3), (117, 1), (117, 2),
(118, 3), (118, 2),
(119, 3), (119, 1), (119, 2),
(120, 3),
(121, 3), (121, 1),
(122, 3), (122, 2),
(123, 3), (123, 1), (123, 2),
(124, 3), (124, 1),
(125, 3),
(126, 3),
(127, 3),
(128, 3), (128, 2),
(129, 3),
(130, 3), (130, 1), (130, 2),
(131, 3),
(132, 3),
(133, 3), (133, 2);
