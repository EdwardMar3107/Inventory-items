INSERT INTO app_users (username, password, role) VALUES
('admin',  '$2a$12$RpGECPVoAYfCEuCNtEh5deqsjxnRhVH2yxTSmRHLYlvECxFyQnRDi', 'ADMINISTRATOR'),
('viewer', '$2a$12$hKCU8GHuV4r4CGtMFVR.mOJ.OHkRrOp9jH4lk6fxJHiJWpUymZpOy', 'VIEWER')
    ON CONFLICT (username) DO NOTHING;