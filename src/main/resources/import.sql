-- Inserindo dados na tabela Escola
INSERT INTO escola (id, nome, capacidade) VALUES (1, 'Escola Primária Central', 50);
INSERT INTO escola (id, nome, capacidade) VALUES (2, 'Colégio Estadual Novo Horizonte', 100);
INSERT INTO escola (id, nome, capacidade) VALUES (3, 'Instituto Técnico Avançado', 75);

-- Inserindo dados na tabela Aluno
INSERT INTO aluno (id, nome) VALUES (1, 'João Silva');
INSERT INTO aluno (id, nome) VALUES (2, 'Maria Oliveira');
INSERT INTO aluno (id, nome) VALUES (3, 'Carlos Souza');
INSERT INTO aluno (id, nome) VALUES (4, 'Ana Costa');
INSERT INTO aluno (id, nome) VALUES (5, 'Pedro Lima');

-- Inserindo dados na tabela Matricula
INSERT INTO matricula (id, curso, escola_id, aluno_id) VALUES (1, 'Matemática Básica', 1, 1);
INSERT INTO matricula (id, curso, escola_id, aluno_id) VALUES (2, 'Português Avançado', 1, 2);
INSERT INTO matricula (id, curso, escola_id, aluno_id) VALUES (3, 'Ciências Naturais', 2, 3);
INSERT INTO matricula (id, curso, escola_id, aluno_id) VALUES (4, 'História Geral', 2, 4);
INSERT INTO matricula (id, curso, escola_id, aluno_id) VALUES (5, 'Programação Java', 3, 5);