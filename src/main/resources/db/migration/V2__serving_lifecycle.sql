-- Existing tasks remain unmanaged until an explicit start/stop/delete request.
-- Unique-key creation fails on duplicates rather than silently deleting records.
ALTER TABLE model_tpl
    MODIFY create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE serve_task
    MODIFY create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD UNIQUE KEY uk_serve_task_name (task_name),
    ADD COLUMN model_id varchar(32) NULL,
    ADD CONSTRAINT fk_serve_model FOREIGN KEY (model_id) REFERENCES model_tpl(id),
    ADD COLUMN gateway_channel_id int NULL,
    ADD COLUMN namespace varchar(63) NULL,
    ADD COLUMN resource_name varchar(63) NULL,
    ADD COLUMN desired_state varchar(16) NOT NULL DEFAULT 'unmanaged',
    ADD COLUMN actual_status varchar(24) NOT NULL DEFAULT 'unknown',
    ADD COLUMN last_error varchar(1000) NULL,
    ADD COLUMN retry_count int NOT NULL DEFAULT 0,
    ADD COLUMN generation bigint NOT NULL DEFAULT 0,
    ADD COLUMN next_retry_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN last_sync_time datetime NULL,
    ADD COLUMN gateway_registered boolean NOT NULL DEFAULT false,
    ADD COLUMN model_name varchar(128) NOT NULL DEFAULT 'model',
    ADD COLUMN gpu_resource varchar(128) NOT NULL DEFAULT 'nvidia.com/gpu',
    ADD KEY idx_serve_reconcile (next_retry_at, task_id),
    ADD KEY idx_serve_type_created (type, create_time, task_id),
    ADD KEY idx_serve_model_code (model_code);
CREATE INDEX idx_model_name ON model_tpl (name);
