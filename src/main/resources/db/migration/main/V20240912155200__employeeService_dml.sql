--Drop constraint pKey
ALTER TABLE eg_employee_data
DROP CONSTRAINT eg_employee_data_pkey;

-- Alter  column type
ALTER TABLE eg_employee_data
ALTER COLUMN empCode TYPE VARCHAR(255);

--Alter column constraint
ALTER TABLE eg_employee_data
ADD PRIMARY KEY (empCode);