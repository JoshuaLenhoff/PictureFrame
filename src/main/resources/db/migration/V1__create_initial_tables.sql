
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                       CONSTRAINT chk_user_role
                           CHECK (role IN ('ADMIN', 'FAMILY'))
);

CREATE TABLE devices (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         api_key VARCHAR(255) NOT NULL UNIQUE,
                         enabled BOOLEAN NOT NULL DEFAULT TRUE,
                         last_sync TIMESTAMPTZ,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE images (
                        id UUID PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        file_name VARCHAR(255) NOT NULL,
                        file_path VARCHAR(500) NOT NULL,
                        content_type VARCHAR(100) NOT NULL,
                        file_size BIGINT NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                        CONSTRAINT fk_image_user
                            FOREIGN KEY (user_id)
                                REFERENCES users(id)
);

CREATE TABLE device_images (
                               id BIGSERIAL PRIMARY KEY,
                               device_id BIGINT NOT NULL,
                               image_id UUID NOT NULL,
                               downloaded_at TIMESTAMPTZ,
                               push_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                               push_duration_seconds INTEGER NOT NULL DEFAULT 120,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                               CONSTRAINT fk_device_image_device
                                   FOREIGN KEY (device_id)
                                       REFERENCES devices(id),

                               CONSTRAINT fk_device_image_image
                                   FOREIGN KEY (image_id)
                                       REFERENCES images(id),

                               CONSTRAINT uq_device_image
                                   UNIQUE (device_id, image_id)
);


CREATE INDEX idx_images_user_id
    ON images(user_id);

CREATE INDEX idx_device_images_device
    ON device_images(device_id);

CREATE INDEX idx_device_images_pending
    ON device_images(device_id, downloaded_at);
