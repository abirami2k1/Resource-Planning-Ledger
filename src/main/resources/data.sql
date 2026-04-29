-- Sample data aligned to current com.rpl entity schema.
-- Idempotent inserts use ON CONFLICT DO NOTHING for reruns.

-- 1) Protocols
INSERT INTO protocol (id, name, description) VALUES
  (1, 'Bridge Construction', 'Standard protocol for constructing a small pedestrian bridge.'),
  (2, 'Road Repair', 'Protocol for patching and resurfacing a road segment.'),
  (3, 'Site Inspection', 'Single-step protocol for inspecting a construction site.')
ON CONFLICT (id) DO NOTHING;

-- 2) Protocol Steps (depends_on is in collection table protocol_step_depends_on)
INSERT INTO protocol_step (id, protocol_id, name) VALUES
  (1, 1, 'Site Survey'),
  (2, 1, 'Foundation Dig'),
  (3, 1, 'Pour Concrete'),
  (4, 1, 'Steel Assembly'),
  (5, 1, 'Final Inspection'),
  (6, 2, 'Traffic Assessment'),
  (7, 2, 'Surface Removal'),
  (8, 2, 'Resurfacing'),
  (9, 2, 'Quality Check'),
  (10, 3, 'Inspect Site')
ON CONFLICT (id) DO NOTHING;

INSERT INTO protocol_step_depends_on (protocol_step_id, depends_on) VALUES
  (2, 'Site Survey'),
  (3, 'Foundation Dig'),
  (4, 'Foundation Dig'),
  (5, 'Pour Concrete'),
  (5, 'Steel Assembly'),
  (7, 'Traffic Assessment'),
  (8, 'Surface Removal'),
  (9, 'Resurfacing')
ON CONFLICT DO NOTHING;

-- 3) Plans
INSERT INTO plan (id, name) VALUES
  (1, 'Riverside Bridge Q2'),
  (2, 'Main Street Road Repair'),
  (3, 'Ad-hoc Inspection Run')
ON CONFLICT (id) DO NOTHING;

-- 4) Resource Types
INSERT INTO resource_type (id, name, kind, unit) VALUES
  (1, 'Engineer', 'ASSET', 'person-day'),
  (2, 'Excavator', 'ASSET', 'machine-day'),
  (3, 'Concrete (m3)', 'CONSUMABLE', 'm3')
ON CONFLICT (id) DO NOTHING;

-- 5) Accounts
INSERT INTO account (id, name, kind, resource_type_id) VALUES
  (1, 'Engineers Pool', 'POOL', 1),
  (2, 'Equipment Pool', 'POOL', 2),
  (3, 'Concrete Pool', 'POOL', 3),
  (4, 'Action-101 Usage', 'USAGE', 1),
  (5, 'Action-102 Usage', 'USAGE', 1),
  (9, 'Over-Consumption Alerts', 'ALERT_MEMO', NULL)
ON CONFLICT (id) DO NOTHING;

-- 6) Proposed Actions
INSERT INTO proposed_action (id, plan_id, name, party, location, status) VALUES
  (101, 1, 'Site Survey', 'Survey Team Alpha', 'Riverside Site', 'COMPLETED'),
  (102, 1, 'Foundation Dig', 'Excavation Crew B', 'Riverside Site', 'COMPLETED'),
  (103, 1, 'Pour Concrete', 'Concrete Crew C', 'Riverside Site', 'IN_PROGRESS'),
  (104, 1, 'Steel Assembly', 'Steel Team D', 'Riverside Site', 'PROPOSED'),
  (105, 1, 'Final Inspection', 'QA Inspector E', 'Riverside Site', 'PROPOSED'),
  (201, 2, 'Traffic Assessment', 'Traffic Analyst F', 'Main St Segment', 'PROPOSED'),
  (202, 2, 'Surface Removal', 'Road Crew G', 'Main St Segment', 'PROPOSED'),
  (203, 2, 'Resurfacing', 'Paving Team H', 'Main St Segment', 'PROPOSED'),
  (204, 2, 'Quality Check', 'QA Inspector E', 'Main St Segment', 'PROPOSED'),
  (301, 3, 'Inspect Riverside Bridge Site', 'Inspector I', 'Riverside Site', 'PROPOSED')
ON CONFLICT (id) DO NOTHING;

-- 7) Implemented Actions
INSERT INTO implemented_action (id, proposed_action_id, actual_start, actual_party, actual_location) VALUES
  (1, 101, '2026-04-01T08:00:00Z', 'Survey Team Alpha', 'Riverside Site'),
  (2, 102, '2026-04-06T07:30:00Z', 'Excavation Crew B', 'Riverside Site'),
  (3, 103, '2026-04-13T08:00:00Z', 'Concrete Crew C', 'Riverside Site')
ON CONFLICT (id) DO NOTHING;

-- 8) Resource Allocations
INSERT INTO resource_allocation (id, action_id, resource_type_id, quantity, kind, asset_id) VALUES
  (1, 101, 1, 2.0, 'GENERAL', NULL),
  (2, 101, 2, 1.0, 'SPECIFIC', 'EXCAVATOR-007'),
  (3, 102, 1, 5.0, 'GENERAL', NULL),
  (4, 102, 2, 3.0, 'SPECIFIC', 'EXCAVATOR-007'),
  (5, 102, 3, 20.0, 'GENERAL', NULL),
  (6, 103, 1, 4.0, 'GENERAL', NULL),
  (7, 103, 3, 50.0, 'GENERAL', NULL),
  (8, 104, 1, 6.0, 'GENERAL', NULL),
  (9, 104, 2, 2.0, 'SPECIFIC', 'CRANE-002'),
  (10, 105, 1, 1.0, 'GENERAL', NULL)
ON CONFLICT (id) DO NOTHING;

-- 9) Ledger transactions
INSERT INTO ledger_transaction (id, description, created_at) VALUES
  (1, 'Completion of: Site Survey (action 101)', '2026-04-03T17:00:00Z'),
  (2, 'Completion of: Foundation Dig (action 102)', '2026-04-10T16:30:00Z'),
  (100, 'Initial pool seed - Engineers', '2026-03-31T00:00:00Z'),
  (101, 'Initial pool seed - Equipment', '2026-03-31T00:00:00Z'),
  (102, 'Initial pool seed - Concrete', '2026-03-31T00:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- 10) Ledger entries
INSERT INTO entry (id, transaction_id, account_id, amount, charged_at, booked_at) VALUES
  (1, 1, 1, -2.0, '2026-04-03T17:00:00Z', '2026-04-03T17:00:01Z'),
  (2, 1, 4,  2.0, '2026-04-03T17:00:00Z', '2026-04-03T17:00:01Z'),
  (3, 1, 2, -1.0, '2026-04-03T17:00:00Z', '2026-04-03T17:00:01Z'),
  (4, 1, 4,  1.0, '2026-04-03T17:00:00Z', '2026-04-03T17:00:01Z'),
  (5, 2, 1, -5.0, '2026-04-10T16:30:00Z', '2026-04-10T16:30:01Z'),
  (6, 2, 5,  5.0, '2026-04-10T16:30:00Z', '2026-04-10T16:30:01Z'),
  (7, 2, 2, -3.0, '2026-04-10T16:30:00Z', '2026-04-10T16:30:01Z'),
  (8, 2, 5,  3.0, '2026-04-10T16:30:00Z', '2026-04-10T16:30:01Z'),
  (100, 100, 1, 30.0, '2026-03-31T00:00:00Z', '2026-03-31T00:00:00Z'),
  (101, 101, 2, 10.0, '2026-03-31T00:00:00Z', '2026-03-31T00:00:00Z'),
  (102, 102, 3, 100.0, '2026-03-31T00:00:00Z', '2026-03-31T00:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- Keep PostgreSQL identity sequences in sync for manually set IDs.
SELECT setval('protocol_id_seq', (SELECT COALESCE(MAX(id), 1) FROM protocol), true);
SELECT setval('protocol_step_id_seq', (SELECT COALESCE(MAX(id), 1) FROM protocol_step), true);
SELECT setval('plan_id_seq', (SELECT COALESCE(MAX(id), 1) FROM plan), true);
SELECT setval('resource_type_id_seq', (SELECT COALESCE(MAX(id), 1) FROM resource_type), true);
SELECT setval('account_id_seq', (SELECT COALESCE(MAX(id), 1) FROM account), true);
SELECT setval('proposed_action_id_seq', (SELECT COALESCE(MAX(id), 1) FROM proposed_action), true);
SELECT setval('implemented_action_id_seq', (SELECT COALESCE(MAX(id), 1) FROM implemented_action), true);
SELECT setval('resource_allocation_id_seq', (SELECT COALESCE(MAX(id), 1) FROM resource_allocation), true);
SELECT setval('ledger_transaction_id_seq', (SELECT COALESCE(MAX(id), 1) FROM ledger_transaction), true);
SELECT setval('entry_id_seq', (SELECT COALESCE(MAX(id), 1) FROM entry), true);
