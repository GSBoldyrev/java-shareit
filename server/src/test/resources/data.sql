INSERT INTO USERS (NAME, EMAIL)
VALUES ('Alfa', 'alfa@yandex.ru'),
       ('Beta', 'beta@yandex.ru'),
       ('Delta', 'delta@yandex.ru');

INSERT INTO REQUESTS (DESCRIPTION, REQUESTOR_ID, CREATED)
VALUES ('Хочу отвертку', 1, '2022-11-25 07:07:07'),
       ('Хочу дрель', 2, '2022-11-24 08:08:08');

INSERT INTO ITEMS (NAME, DESCRIPTION, AVAILABLE, OWNER_ID, REQUEST_ID)
VALUES ('Пила', 'Очень острая', true, 1, null),
       ('Молоток', 'Огромный', true, 1, null),
       ('Отвертка', 'Маленькая минус', true, 2, 1),
       ('Ключ', 'Молоток в комплекте', true, 3, null),
       ('Дрель', 'И перфоратор', false, 3, 2);

INSERT INTO BOOKINGS (START_TIME, END_TIME, ITEM_ID, BOOKER_ID, STATUS)
VALUES ('2021-11-10 07:07:07', '2021-11-12 07:07:07', 1, 2, 1),
       ('2023-11-27 07:07:07', '2023-11-29 07:07:07', 1, 3, 0),
       ('2021-11-14 07:07:07', '2021-11-15 07:07:07', 3, 1, 1),
       ('2023-12-30 07:07:07', '2023-12-30 08:07:07', 5, 1, 0),
       ('2022-11-13 07:07:07', '2022-11-15 07:07:07', 1, 3, 1),
       ('2023-11-25 07:07:07', '2023-11-25 08:07:07', 2, 2, 2);

INSERT INTO COMMENTS (TEXT, AUTHOR_ID, ITEM_ID, CREATED)
VALUES ('Класс!', 2, 1, '2022-11-13 07:07:07'),
       ('Неее!', 3, 1, '2022-11-16 07:07:07'),
       ('Понравилось!', 1, 3, '2022-11-17 07:07:07');
