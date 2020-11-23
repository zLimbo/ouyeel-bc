SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tx
-- ----------------------------
DROP TABLE IF EXISTS tx;
CREATE TABLE tx (
  systemId varchar(255),
  requestSn varchar(255),
  dataInfo text,
  secretKey varchar(255),
  privateKey varchar(255),
  publicKey varchar(255),
  txHash varchar(255),
  onChain varchar(255),
  PRIMARY KEY (systemId, requestSn)
);

