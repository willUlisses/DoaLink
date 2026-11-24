ALTER TABLE usuarios ADD COLUMN email_contato VARCHAR(255);
UPDATE usuarios SET email_contato = email WHERE email_contato IS NULL;
