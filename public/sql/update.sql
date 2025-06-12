
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

CREATE TABLE public.kategori (
   kategori_id serial4 NOT NULL,
   name varchar(255) DEFAULT '-' NOT NULL,
   created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
   updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
   is_delete boolean DEFAULT false NOT NULL,
   CONSTRAINT kategori_pk PRIMARY KEY (kategori_id)
);

CREATE TABLE public.barang (
  barang_id serial4 NOT NULL,
  name varchar(255) DEFAULT '-' NOT NULL,
  kategori_id INT,
  harga DECIMAL(20),
  stok int,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  is_delete boolean DEFAULT false NOT NULL,
  CONSTRAINT barang_pk PRIMARY KEY (barang_id),
  CONSTRAINT barang_kategori_fk FOREIGN KEY (kategori_id) REFERENCES public.kategori(kategori_id) ON DELETE CASCADE ON UPDATE CASCADE
);
