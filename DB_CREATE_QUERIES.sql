create table user (
    id int primary key auto_increment,
    username varchar(50) not null,
    password varchar(50) not null,
    unique (username),
    index (username, id)
);

create table grp (
    id int primary key auto_increment,
    name varchar(50) not null,
    created_user int references user(id),
    unique(name),
    index (name, id)
);

create table user_grp_map (
    user_id int not null references user(id) on delete cascade,
    grp_id int not null references grp(id) on delete cascade
);

create table expense (
    id int primary key auto_increment,
    name varchar(50) not null,
    grp_id int references grp(id),
    created_user int references user(id),
    amount int not null,
    created_at timestamp not null default current_timestamp,
);

create table share (
    id int primary key auto_increment,
    expense_id int references expense(id),
    assigned_to int references user(id),
    total_amount int not null,
    paid_amount int default 0,
    is_paid boolean default false
);
