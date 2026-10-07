CREATE TABLE IF not EXISTS prodotti  (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    descrizione TEXT,
    prezzo DECIMAL(10,2) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    quantita INT NOT NULL DEFAULT 0,
    data_creazione TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO prodotti
(nome, descrizione, prezzo, categoria, quantita, data_creazione)
VALUES
(
    'Laptop Pro 15',
    'Notebook professionale con processore Intel Core i7, 16 GB di RAM e SSD da 512 GB.',
    1299.90,
    'Informatica',
    15,
    '2026-09-01 09:00:00'
),
(
    'Laptop Air 13',
    'Notebook compatto e leggero con 8 GB di RAM e SSD da 256 GB.',
    899.00,
    'Informatica',
    22,
    '2026-09-02 10:15:00'
),
(
    'Monitor 27 4K',
    'Monitor professionale da 27 pollici con risoluzione 4K UHD.',
    449.90,
    'Informatica',
    12,
    '2026-09-03 11:30:00'
),
(
    'Tastiera Meccanica RGB',
    'Tastiera meccanica con retroilluminazione RGB e switch programmabili.',
    89.90,
    'Accessori',
    35,
    '2026-09-04 14:00:00'
),
(
    'Mouse Wireless Pro',
    'Mouse wireless ergonomico con sensore ad alta precisione.',
    59.90,
    'Accessori',
    48,
    '2026-09-05 09:45:00'
),
(
    'Cuffie Bluetooth',
    'Cuffie wireless con cancellazione attiva del rumore.',
    149.90,
    'Audio',
    18,
    '2026-09-06 15:20:00'
),
(
    'Webcam Full HD',
    'Webcam Full HD 1080p con microfono integrato.',
    69.90,
    'Accessori',
    27,
    '2026-09-07 10:10:00'
),
(
    'Microfono USB Studio',
    'Microfono USB a condensatore per streaming, podcast e videoconferenze.',
    119.00,
    'Audio',
    14,
    '2026-09-08 13:40:00'
),
(
    'SSD Esterno 1TB',
    'Unità SSD esterna USB 3.2 con capacità di 1 TB.',
    109.90,
    'Storage',
    31,
    '2026-09-09 16:00:00'
),
(
    'Hard Disk Esterno 4TB',
    'Hard disk esterno USB con capacità di 4 TB.',
    129.90,
    'Storage',
    19,
    '2026-09-10 09:30:00'
),
(
    'Tablet 11',
    'Tablet da 11 pollici con 128 GB di memoria interna.',
    399.90,
    'Mobile',
    16,
    '2026-09-11 11:00:00'
),
(
    'Smartphone X',
    'Smartphone con display OLED, 256 GB di memoria e fotocamera avanzata.',
    799.90,
    'Mobile',
    20,
    '2026-09-12 12:15:00'
),
(
    'Smartwatch Active',
    'Smartwatch per monitoraggio attività fisica e notifiche.',
    249.90,
    'Wearable',
    25,
    '2026-09-13 14:30:00'
),
(
    'Router WiFi 6',
    'Router wireless WiFi 6 per reti domestiche e piccoli uffici.',
    159.90,
    'Networking',
    17,
    '2026-09-14 10:00:00'
),
(
    'Switch Gigabit 8 Porte',
    'Switch Ethernet Gigabit a 8 porte.',
    49.90,
    'Networking',
    40,
    '2026-09-15 15:45:00'
),
(
    'Stampante Laser',
    'Stampante laser monocromatica per uso professionale.',
    219.90,
    'Ufficio',
    9,
    '2026-09-16 09:20:00'
),
(
    'Sedia Ergonomica',
    'Sedia da ufficio ergonomica con supporto lombare regolabile.',
    329.90,
    'Ufficio',
    11,
    '2026-09-17 13:10:00'
),
(
    'Scrivania Regolabile',
    'Scrivania elettrica regolabile in altezza.',
    549.90,
    'Ufficio',
    7,
    '2026-09-18 10:30:00'
),
(
    'Power Bank 20000mAh',
    'Batteria portatile ad alta capacità con ricarica rapida.',
    39.90,
    'Accessori',
    55,
    '2026-09-19 16:20:00'
),
(
    'Hub USB-C',
    'Hub USB-C multiporta con HDMI, USB 3.0 e lettore SD.',
    74.90,
    'Accessori',
    29,
    '2026-09-20 11:45:00'
);