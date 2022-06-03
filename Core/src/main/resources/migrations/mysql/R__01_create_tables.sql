create table if not exists ${prefix}plot(
    id        int(11)     not null auto_increment,
    plot_id_x int(11)     not null,
    plot_id_z int(11)     not null,
    world     varchar(45) not null,
    owner     varchar(40) not null,
    timestamp timestamp   not null default current_timestamp,
    primary key (id)
) engine=InnoDB default charset=utf8 auto_increment=0;

-- TODO: Migrating existing data to this table.
create table if not exists ${prefix}plot_role(
    plot_id int(11)                             not null,
    user_id varchar(45)                         not null,
    role    enum('helper', 'trusted', 'denied') not null,
    foreign key (plot_id) references ${prefix}plot(id) on delete cascade,
    primary key (plot_id, user_id)
) engine=InnoDB default charset=utf8 auto_increment=0;

create table if not exists ${prefix}plot_comments(
    world     varchar(45) not null,
    comment   varchar(45) not null,
    inbox     varchar(45) not null,
    timestamp int(11)     not null,
    sender    varchar(45) not null
) engine=InnoDB default charset=utf8 auto_increment=0;

-- TODO: Look into what to do with this one...
--  Most data is now found in flags.
create table if not exists ${prefix}plot_settings(
    plot_plot_id  int(11)       not null,
    biome         varchar(45)   default 'FOREST',  -- Unused. Moved to flags.
    rain          int(1)        default 0,         -- Unused. Moved to flags.
    custom_time   tinyint(1)    default 0,         -- Unused. Moved to flags.
    time          int(11)       default 8000,      -- Unused. Moved to flags.
    deny_entry    tinyint(1)    default 0,         -- Unused. Moved to flags.
    alias         varchar(50)   default null,
    merged        int(11)       default null,
    position      varchar(50)   not null default 'default',
    foreign key (plot_plot_id) references ${prefix}plot(id) on delete cascade,
    primary key (plot_plot_id)
) engine=InnoDB default charset=utf8;

-- TODO: Look into adding foreign keys to this.
create table if not exists ${prefix}plot_rating(
    plot_plot_id    int(11)     not null,
    rating          int(2)      not null,
    player          varchar(45) not null
) engine=InnoDB default charset=utf8;

-- TODO: Drop the key and make the player ID the key instead.
create table if not exists ${prefix}player_meta(
    meta_id int(11) not null auto_increment,
    uuid    varchar(45) not null,
    key     varchar(32) not null,
    value   blob not null,
    primary key (meta_id)
) engine=InnoDB default charset=utf8 auto_increment=0;

-- TODO: Drop the ID and make (plot_id, flag) the new primary key.
create table if not exists ${prefix}plot_flags(
    id int(11) not null auto_increment primary key,
    plot_id int(11) not null,
    flag varchar(64),
    value varchar(512),
    foreign key (plot_id) references ${prefix}plot(id) on delete cascade,
    unique (plot_id, flag)
) engine=InnoDB default charset=utf8 auto_increment=0;
