INSERT INTO app_users (username, password, role) VALUES
    ('admin',  '$2b$12$O.odQUwlPDEiYqjffLFHNemL8X9iKx98OL.6BCEEfTpxwX7JwwKzm', 'ADMINISTRATOR'),
    ('viewer', '$2b$12$VYpx0j4Q80I96KT1K86dn.VvBIiOOgCljAYRa78GqoKDVzunmWujq', 'VIEWER')
    ON CONFLICT (username) DO NOTHING;