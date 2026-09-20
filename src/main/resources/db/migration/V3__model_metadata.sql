-- Keep model metadata independent from deployment storage configuration.
ALTER TABLE model_tpl
    ADD COLUMN version varchar(64) NULL COMMENT '模型版本',
    ADD COLUMN description varchar(1000) NULL COMMENT '模型描述',
    ADD COLUMN image varchar(255) NULL COMMENT '默认镜像',
    ADD COLUMN cmd varchar(1000) NULL COMMENT '启动命令';
