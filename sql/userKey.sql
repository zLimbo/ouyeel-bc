
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for userKey
-- ----------------------------
DROP TABLE IF EXISTS `userKey`;
CREATE TABLE `userKey`  (
  `systemId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `privateKey` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `publicKey` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`systemId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of userKey
-- ----------------------------
INSERT INTO `userKey` VALUES ('000001', '0x67d7273b1e670ca5b0482381b631cee28a33ac03d8839244ae97df6f74bc027d', '0x0379f6feff204503fd71e6ecb16b1f190d70aae14358ac79e2739fcc2779ecc18ec8e25f861839a8607dc941ddc6c75116b89d7a2cbd6f23189d2265ebb4edd7');
INSERT INTO `userKey` VALUES ('000002', '0x071d5a55d71ea3e27c1dec01d88b605e4460b0d81b149173a2e415eca6a940fc', '0x8dfb55834184655669e0b4302f61177209e1d57c9e557d0ee314de1cd91fdbb184ee3ebca284b1c21dcb912c2012a65520f6e5dc17f771f32d3a421a88ea9cfd');
INSERT INTO `userKey` VALUES ('000003', '0x26995c021543ea9f7544dc70e850601fb3701db5ba605fa5c6268f3466b22c0f', '0xe7b605d4ba74058fb82b38c064fd3e81b2b85e66f4ba3fe1579ef6fb4cc2929efd2fd38a74ce85a51d55c0fa96566e53ff154458a7829c29b1dd883ca7a73a0b');

SET FOREIGN_KEY_CHECKS = 1;
