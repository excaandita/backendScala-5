
CREATE TABLE public.mahasiswa (
    mahasiswa_id serial4 NOT NULL,
    name varchar(255) DEFAULT '-' NOT NULL,
    email varchar(150),
    telephone varchar(20),
    address TEXT,
    start_date DATE NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_delete boolean DEFAULT false NOT NULL
);
