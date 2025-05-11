create extension if not exists vector ;
create extension if not exists hstore ;

create extension if not exists "uuid-ossp" ;

-- drop table   vector_store;

create table  if not exists vector_store(
    id uuid default uuid_generate_v4() primary key ,
    content text,
    metadata json ,
    embedding vector(1536)
);

create index on vector_store using hnsw(embedding vector_cosine_ops);