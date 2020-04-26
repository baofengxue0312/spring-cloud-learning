-- seata biz
create database seata_order;
USE seata_order;
CREATE TABLE `t_order`  (
    `id` bigint(11) NOT NULL AUTO_INCREMENT PRIMARY KEY ,
    `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
    `product_id` bigint(11) DEFAULT NULL COMMENT '产品id',
    `count` int(11) DEFAULT NULL COMMENT '数量',
    `money` decimal(11, 0) DEFAULT NULL COMMENT '金额',
    `status` int(1) DEFAULT NULL COMMENT '订单状态:  0:创建中 1:已完结'
) ENGINE = InnoDB AUTO_INCREMENT=7 CHARACTER SET = utf8;

-- the table to store seata xid data
-- 0.7.0+ add context
-- you must to init this sql for you business databese. the seata server not need it.
-- 此脚本必须初始化在你当前的业务数据库中，用于AT 模式XID记录。与server端无关（注：业务数据库）
-- 注意此处0.3.0+ 增加唯一索引 ux_undo_log
drop table if EXISTS `undo_log`;
CREATE TABLE `undo_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `branch_id` bigint(20) NOT NULL,
    `xid` varchar(100) NOT NULL,
    `context` varchar(128) NOT NULL,
    `rollback_info` longblob NOT NULL,
    `log_status` int(11) NOT NULL,
    `log_created` datetime NOT NULL,
    `log_modified` datetime NOT NULL,
    `ext` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

create database seata_storage;
USE seata_storage;
DROP TABLE IF EXISTS `t_storage`;
CREATE TABLE `t_storage`  (
    `id` bigint(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `product_id` bigint(11) DEFAULT NULL COMMENT '产品id',
    `total` int(11) DEFAULT NULL COMMENT '总库存',
    `used` int(11) DEFAULT NULL COMMENT '已用库存',
    `residue` int(11) DEFAULT NULL COMMENT '剩余库存'
) ENGINE = InnoDB AUTO_INCREMENT=2 CHARACTER SET = utf8;

INSERT INTO `t_storage` VALUES (1, 1, 100, 0, 100);

-- the table to store seata xid data
-- 0.7.0+ add context
-- you must to init this sql for you business databese. the seata server not need it.
-- 此脚本必须初始化在你当前的业务数据库中，用于AT 模式XID记录。与server端无关（注：业务数据库）
-- 注意此处0.3.0+ 增加唯一索引 ux_undo_log
drop table if EXISTS `undo_log`;
CREATE TABLE `undo_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `branch_id` bigint(20) NOT NULL,
    `xid` varchar(100) NOT NULL,
    `context` varchar(128) NOT NULL,
    `rollback_info` longblob NOT NULL,
    `log_status` int(11) NOT NULL,
    `log_created` datetime NOT NULL,
    `log_modified` datetime NOT NULL,
    `ext` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE database seata_account;
USE seata_account;
DROP TABLE IF EXISTS `t_account`;
CREATE TABLE `t_account`  (
    `id` bigint(11) NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    `user_id` bigint(11) DEFAULT NULL COMMENT '用户id',
    `total` decimal(10, 0) DEFAULT NULL COMMENT '总额度',
    `used` decimal(10, 0) DEFAULT NULL COMMENT '已用余额',
    `residue` decimal(10, 0) DEFAULT NULL COMMENT '剩余可用额度'
) ENGINE = InnoDB AUTO_INCREMENT=2 CHARACTER SET = utf8;

INSERT INTO `t_account` VALUES (1, 1, 1000, 0, 1000);

-- the table to store seata xid data
-- 0.7.0+ add context
-- you must to init this sql for you business databese. the seata server not need it.
-- 此脚本必须初始化在你当前的业务数据库中，用于AT 模式XID记录。与server端无关（注：业务数据库）
-- 注意此处0.3.0+ 增加唯一索引 ux_undo_log
drop table if EXISTS `undo_log`;
CREATE TABLE `undo_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `branch_id` bigint(20) NOT NULL,
    `xid` varchar(100) NOT NULL,
    `context` varchar(128) NOT NULL,
    `rollback_info` longblob NOT NULL,
    `log_status` int(11) NOT NULL,
    `log_created` datetime NOT NULL,
    `log_modified` datetime NOT NULL,
    `ext` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
