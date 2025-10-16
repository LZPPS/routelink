create table if not exists bookings (
  id bigserial primary key,
  trip_id bigint not null references trips(id) on delete cascade,
  rider_id bigint not null references users(id) on delete restrict,
  seats int not null check (seats > 0),
  status varchar(16) not null check (status in ('CONFIRMED','CANCELLED')),
  created_at timestamp not null default now()
);

create index if not exists idx_bookings_trip on bookings(trip_id);
create index if not exists idx_bookings_rider on bookings(rider_id);
