INSERT INTO options VALUES(1, "REGISTER_ALLOWED", "Registration allowance", "Boolean", "true");
INSERT INTO options VALUES(2, "AFTER_PAGE_SCRIPT", "After page script", "String", "<!-- -->");

INSERT INTO roles VALUES(1, "admin", "Administrator");
INSERT INTO roles VALUES(2, "client", "Client");

INSERT INTO permissions VALUES(1, "ref.admin", "Reference admin permission");
INSERT INTO permissions VALUES(2, "ref.client", "Reference client permission");

INSERT INTO permissions_to_targets VALUES(1, "role", 1);
INSERT INTO permissions_to_targets VALUES(2, "role", 1);
INSERT INTO permissions_to_targets VALUES(2, "role", 2);


INSERT INTO accounts VALUES(1, 'admin', 'admin@blockwit.io', '$2a$04$nwNtp5tm0McpKZ88770vUejmWJ5.d8VGBTy0xIh0VQuh.iabAZChq', 'confirmed', 'normal', 1539723600000, NULL, NULL, NULL);

INSERT INTO options VALUES(3, 'ETH_BLOCK_DELAY', 'How much blocks should we wait before accepting event inside them', 'Long', '3');
INSERT INTO options VALUES(4, 'ETH_CHECKED_BLOCK_NUM', 'Last checked block', 'Long', 0);
INSERT INTO options VALUES(5, 'ETH_CONTRACT_ADDRESS', 'Lottery controller address', 'String', '');
INSERT INTO options VALUES(6, 'ETH_NODE_PROVIDER', 'ETH node provider', 'String', 'https://kovan.infura.io/v3/3d757e9351ff488a933d40c02f709310');
INSERT INTO options VALUES(7, 'ETH_START_BLOCK_NUM', 'Start block for event checking', 'Long', 0 );





