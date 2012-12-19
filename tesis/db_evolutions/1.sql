ALTER TABLE EXPERIMENTS ADD COLUMN IF NOT EXISTS "keep_learnts_limit" boolean NOT NULL DEFAULT false;
ALTER TABLE EXPERIMENTS ADD COLUMN IF NOT EXISTS "keep_restarts" boolean NOT NULL DEFAULT false;
ALTER TABLE EXPERIMENTS ADD COLUMN IF NOT EXISTS "keep_learnt_facts" boolean NOT NULL DEFAULT false;

CREATE VIEW TOTAL_TIMES AS select "experiment_id", sum("time") as "total_time" from iterations group by "experiment_id";

CREATE VIEW TIMES_PER_FILTER_AND_LIFTER AS select "cnf", "lifter", "filter", tt."total_time", ct."critical_time" from EXPERIMENTS e JOIN TOTAL_TIMES tt ON e."id" = tt."experiment_id" JOIN CRITICAL_TIMES ct ON e."id" = ct."experiment_id" ORDER BY "cnf", "lifter", "filter";

CREATE VIEW EXPERIMENTS_NOT_STARTED AS select * from EXPERIMENTS e WHERE NOT EXISTS (SELECT * FROM ITERATIONS WHERE "experiment_id" = e."id");
CREATE VIEW EXPERIMENTS_NOT_FINISHED AS select * from EXPERIMENTS e WHERE EXISTS (SELECT * FROM ITERATIONS WHERE "experiment_id" = e."id" AND "time" IS NULL);

--Example of the functions
--CREATE ALIAS RESULTS_FOR_TABLE AS ' ResultSet results_for_table(Connection conn, String cnf, String lifter, int sorder) throws SQLException { String query = "(SELECT * FROM TIMES_PER_FILTER_AND_LIFTER WHERE \"cnf\" LIKE ''%"+cnf+"%'' AND \"lifterLIMIT "+new Integer(sorder * 28 + 0).toString()+", 10) UNION ALL (SELECT * FROM TIMES_PER_FILTER_AND_LIFTER WHERE \"cnf\" LIKE ''%"+cnf+"%'' LIMIT "+new Integer(sorder * 28 + 11).toString()+", 17) UNION ALL (SELECT * FROM TIMES_PER_FILTER_AND_LIFTER WHERE \"cnf\" LIKE ''%"+cnf+"%'' LIMIT "+new Integer(sorder * 28 + 10).toString()+", 1)"; return conn.createStatement().executeQuery(query); } ';