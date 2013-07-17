create table cris_do (id int4 not null, crisID varchar(255) unique, sourceID varchar(255) unique, status bool, uuid varchar(255) not null unique, timestampCreated timestamp, timestampLastModified timestamp, typo_id int4, primary key (id));
create table cris_do_box (id int4 not null, collapsed bool not null, externalJSP varchar(255), priority int4 not null, shortName varchar(255) unique, title varchar(255), unrelevant bool not null, visibility int4, primary key (id));
create table cris_do_box2con (cris_do_box_id int4 not null, jdyna_containable_id int4 not null);
create table cris_do_etab (id int4 not null, ext varchar(255), mandatory bool not null, mime varchar(255), priority int4 not null, shortName varchar(255) unique, title varchar(255), visibility int4, displayTab_id int4, primary key (id));
create table cris_do_etab2box (cris_do_etab_id int4 not null, cris_do_box_id int4 not null);
create table cris_do_no (id int4 not null, endDate timestamp, startDate timestamp, positionDef int4, status bool, timestampCreated timestamp, timestampLastModified timestamp, uuid varchar(255) not null unique, scopeDef_id int4, parent_id int4, typo_id int4, primary key (id), unique (positionDef, typo_id, parent_id));
create table cris_do_no_pdef (id int4 not null, accessLevel int4, advancedSearch bool not null, fieldmin_col int4, fieldmin_col_unit varchar(255), fieldmin_row_unit varchar(255), fieldmin_row int4, help text, label varchar(255), labelMinSize int4 not null, labelMinSizeUnit varchar(255), mandatory bool not null, newline bool not null, onCreation bool, priority int4 not null, repeatable bool not null, shortName varchar(255) unique, showInList bool not null, simpleSearch bool not null, rendering_id int4 unique, primary key (id));
create table cris_do_no_prop (id int4 not null, endDate timestamp, startDate timestamp, lockDef int4, positionDef int4 not null, visibility int4, scopeDef_id int4, value_id int4 unique, parent_id int4, typo_id int4, primary key (id), unique (positionDef, typo_id, parent_id));
create table cris_do_no_tp (id int4 not null, label varchar(255), shortName varchar(255), accessLevel int4, help varchar(4000), inline bool not null, mandatory bool not null, newline bool not null, priority int4 not null, repeatable bool not null, primary key (id));
create table cris_do_no_tp2pdef (cris_do_no_tp_id int4 not null, cris_do_no_pdef_id int4 not null);
create table cris_do_pdef (id int4 not null, accessLevel int4, advancedSearch bool not null, fieldmin_col int4, fieldmin_col_unit varchar(255), fieldmin_row_unit varchar(255), fieldmin_row int4, help text, label varchar(255), labelMinSize int4 not null, labelMinSizeUnit varchar(255), mandatory bool not null, newline bool not null, onCreation bool, priority int4 not null, repeatable bool not null, shortName varchar(255) unique, showInList bool not null, simpleSearch bool not null, rendering_id int4 unique, primary key (id));
create table cris_do_prop (id int4 not null, endDate timestamp, startDate timestamp, lockDef int4, positionDef int4 not null, visibility int4, scopeDef_id int4, value_id int4 unique, parent_id int4, typo_id int4, primary key (id), unique (positionDef, typo_id, parent_id));
create table cris_do_tab (id int4 not null, ext varchar(255), mandatory bool not null, mime varchar(255), priority int4 not null, shortName varchar(255) unique, title varchar(255), visibility int4, primary key (id));
create table cris_do_tab2box (cris_do_tab_id int4 not null, cris_do_box_id int4 not null);
create table cris_do_tp (id int4 not null, label varchar(255), shortName varchar(255), primary key (id));
create table cris_do_tp2pdef (cris_do_tp_id int4 not null, cris_do_pdef_id int4 not null);
alter table jdyna_containable add column cris_do_no_tp_fk int4;
alter table jdyna_containable add column cris_do_no_pdef_fk int4;
alter table jdyna_containable add column cris_do_pdef_fk int4;
alter table cris_do add constraint FK3D8EBCB1AFBC4533 foreign key (typo_id) references cris_do_tp;
alter table cris_do_box2con add constraint FK2532FECD1C4ABF89 foreign key (jdyna_containable_id) references jdyna_containable;
alter table cris_do_box2con add constraint FK2532FECDF2FA5297 foreign key (cris_do_box_id) references cris_do_box;
alter table cris_do_etab add constraint FKDBAEBDEC23AF2F7 foreign key (displayTab_id) references cris_do_tab;
alter table cris_do_etab2box add constraint FK9C9E0537F2FA5297 foreign key (cris_do_box_id) references cris_do_box;
alter table cris_do_etab2box add constraint FK9C9E053776BE0976 foreign key (cris_do_etab_id) references cris_do_etab;
alter table cris_do_no add constraint FKC3CB15F7A7AEA5D28579ac0f foreign key (scopeDef_id) references jdyna_scopedefinition;
alter table cris_do_no add constraint FK8579AC0FF32D30FE foreign key (typo_id) references cris_do_no_tp;
alter table cris_do_no add constraint FK8579AC0F3B9ABFA7 foreign key (parent_id) references cris_do;
alter table cris_do_no_prop add constraint FKC8A841F5A7AEA5D29eb9e193 foreign key (scopeDef_id) references jdyna_scopedefinition;
alter table cris_do_no_prop add constraint FK9EB9E193331D8A0B foreign key (typo_id) references cris_do_no_pdef;
alter table cris_do_no_prop add constraint FKC8A841F5E52079D79eb9e193 foreign key (value_id) references jdyna_values;
alter table cris_do_no_prop add constraint FK9EB9E193548C257E foreign key (parent_id) references cris_do_no;
create index cris_do_no_pprop_idx on cris_do_no_prop (parent_id);
alter table cris_do_no_tp2pdef add constraint FKEE06779B6EAF91EA foreign key (cris_do_no_pdef_id) references cris_do_no_pdef;
alter table cris_do_no_tp2pdef add constraint FKEE06779B5EBB4E96 foreign key (cris_do_no_tp_id) references cris_do_no_tp;
alter table cris_do_prop add constraint FKC8A841F5A7AEA5D2dbfe631 foreign key (scopeDef_id) references jdyna_scopedefinition;
alter table cris_do_prop add constraint FKDBFE631BBAA874 foreign key (typo_id) references cris_do_pdef;
alter table cris_do_prop add constraint FKC8A841F5E52079D7dbfe631 foreign key (value_id) references jdyna_values;
alter table cris_do_prop add constraint FKDBFE6313B9ABFA7 foreign key (parent_id) references cris_do;
create index cris_do_pprop_idx on cris_do_prop (parent_id);
alter table cris_do_tab2box add constraint FKC44947E09A14BB03 foreign key (cris_do_tab_id) references cris_do_tab;
alter table cris_do_tab2box add constraint FKC44947E0F2FA5297 foreign key (cris_do_box_id) references cris_do_box;
alter table cris_do_tp2pdef add constraint FKDB59C63D349FFEF5 foreign key (cris_do_pdef_id) references cris_do_pdef;
alter table cris_do_tp2pdef add constraint FKDB59C63D8D04F1ED foreign key (cris_do_tp_id) references cris_do_tp;
alter table jdyna_containable add constraint FK504277E1349FFE9F foreign key (cris_do_pdef_fk) references cris_do_pdef;
alter table jdyna_containable add constraint FK504277E15EBB4E40 foreign key (cris_do_no_tp_fk) references cris_do_no_tp;
alter table jdyna_containable add constraint FK504277E16EAF9194 foreign key (cris_do_no_pdef_fk) references cris_do_no_pdef;
create sequence CRIS_DYNAOBJ_SEQ;
create sequence CRIS_TYPODYNAOBJ_SEQ;
alter table cris_do_box add column typeDef_id int4;
alter table cris_do_etab add column typeDef_id int4;
alter table cris_do_tab add column typeDef_id int4;
alter table cris_do_box add constraint FK29BBA93D1ED73E00 foreign key (typeDef_id) references cris_do_tp;
alter table cris_do_etab add constraint FKDBAEBDE1ED73E00 foreign key (typeDef_id) references cris_do_tp;
alter table cris_do_tab add constraint FK29BBEB071ED73E00 foreign key (typeDef_id) references cris_do_tp;
create table cris_do_wfile (id int4 not null, fileDescription text, labelAnchor varchar(255), showPreview bool not null, widgetSize int4, useInStatistics bool not null, primary key (id));
create table cris_do_tp2notp (cris_do_tp_id int4 not null, cris_do_no_tp_id int4 not null);
alter table cris_do_tp2notp add constraint FKDB5908A55EBB4E96 foreign key (cris_do_no_tp_id) references cris_do_no_tp;
alter table cris_do_tp2notp add constraint FKDB5908A51EEE761 foreign key (cris_do_tp_id) references cris_do_tp;
alter table jdyna_values add column dovalue int4;
alter table jdyna_values add constraint FK51AA118F8565BC6B foreign key (dovalue) references cris_do;
create table cris_do_wpointer (id int4 not null, display text, filtro text, indexName varchar(255), widgetSize int4, target varchar(255), filterExtended text, primary key (id));
create table cris_ou_wpointer (id int4 not null, display text, filtro text, indexName varchar(255), widgetSize int4, target varchar(255), primary key (id));
create table cris_pj_wpointer (id int4 not null, display text, filtro text, indexName varchar(255), widgetSize int4, target varchar(255), primary key (id));
create table cris_rp_wpointer (id int4 not null, display text, filtro text, indexName varchar(255), widgetSize int4, target varchar(255), primary key (id));
insert into cris_rp_wpointer
(id, display, filtro, indexname, widgetsize, target)
SELECT id, display, filtro, indexname, widgetsize, target
  FROM jdyna_widget_pointer where target = 'org.dspace.app.cris.model.jdyna.value.RPPointer';
insert into cris_pj_wpointer
(id, display, filtro, indexname, widgetsize, target)
SELECT id, display, filtro, indexname, widgetsize, target
  FROM jdyna_widget_pointer where target = 'org.dspace.app.cris.model.jdyna.value.ProjectPointer';
insert into cris_ou_wpointer
(id, display, filtro, indexname, widgetsize, target)
SELECT id, display, filtro, indexname, widgetsize, target
  FROM jdyna_widget_pointer where target = 'org.dspace.app.cris.model.jdyna.value.OUPointer';
DROP table jdyna_widget_pointer; 
 