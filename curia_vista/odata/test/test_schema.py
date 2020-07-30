from unittest import TestCase

from odata.odata import create_parser
from odata.sql import to_schema
from odata.topology import get_topology

XML_SCHEMA_PRE = """<?xml version="1.0" encoding="utf-8"?>
    <edmx:Edmx xmlns:edmx="http://schemas.microsoft.com/ado/2007/06/edmx" Version="1.0">
      <edmx:DataServices xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" m:DataServiceVersion="1.0" m:MaxDataServiceVersion="2.0">
        <Schema xmlns="http://schemas.microsoft.com/ado/2009/11/edm" Namespace="itsystems.Pd.DataServices.DataModel">
    """
XML_SCHEMA_POST = """
        </Schema>
      </edmx:DataServices>
    </edmx:Edmx>
    """


class TestSchemaToSQL(TestCase):
    def test_to_schema_real_world_member_party(self):
        xml = XML_SCHEMA_PRE + """
              <EntityType Name="MemberParty">
                <Key>
                  <PropertyRef Name="ID"/>
                  <PropertyRef Name="Language"/>
                </Key>
                <Property Name="ID" Type="Edm.Int32" Nullable="false"/>
                <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
                <Property Name="PartyNumber" Type="Edm.Int32" Nullable="false"/>
                <Property Name="PartyName" Type="Edm.String" MaxLength="Max" FixedLength="false" Unicode="true"/>
                <Property Name="PersonNumber" Type="Edm.Int32" Nullable="false"/>
                <Property Name="PersonIdCode" Type="Edm.Int32" Nullable="true"/>
                <Property Name="FirstName" Type="Edm.String" MaxLength="40" FixedLength="false" Unicode="true"/>
                <Property Name="LastName" Type="Edm.String" MaxLength="60" FixedLength="false" Unicode="true"/>
                <Property Name="GenderAsString" Type="Edm.String" Nullable="false" MaxLength="1" FixedLength="false" Unicode="false"/>
                <Property Name="PartyFunction" Type="Edm.String" MaxLength="80" FixedLength="false" Unicode="true"/>
                <Property Name="Modified" Type="Edm.DateTime" Precision="3"/>
                <Property Name="PartyAbbreviation" Type="Edm.String" MaxLength="Max" FixedLength="false" Unicode="true"/>
                <NavigationProperty Name="Parties" Relationship="itsystems.Pd.DataServices.DataModel.PartyMemberParty" ToRole="Party" FromRole="MemberParty"/>
                <NavigationProperty Name="MembersCouncil" Relationship="itsystems.Pd.DataServices.DataModel.MemberCouncilMemberParty" ToRole="MemberCouncil" FromRole="MemberParty"/>
              </EntityType>
        """ + XML_SCHEMA_POST

        self.assertEqual(
            "CREATE TABLE member_party (\n"
            "  id integer NOT NULL,\n"
            "  language char(2) NOT NULL,\n"
            "  party_number integer NOT NULL,\n"
            "  party_name TEXT,\n"
            "  person_number integer NOT NULL,\n"
            "  person_id_code integer,\n"
            "  first_name varchar(40),\n"
            "  last_name varchar(60),\n"
            "  gender_as_string varchar(1) NOT NULL,\n"
            "  party_function varchar(80),\n"
            "  modified timestamp,\n"
            "  party_abbreviation TEXT,\n"
            "  PRIMARY KEY (id, language)\n"
            ");",
            to_schema(xml))

    def test_to_schema_replace_keywords(self):
        xml = XML_SCHEMA_PRE + """
              <EntityType Name="Keywords">
                <Key>
                  <PropertyRef Name="ID"/>
                </Key>
                <Property Name="ID" Type="Edm.Guid"/>
                <Property Name="Start" Type="Edm.DateTime"/>
                <Property Name="End" Type="Edm.DateTime"/>
                <Property Name="PrefixedStart" Type="Edm.DateTime"/>
              </EntityType>
        """ + XML_SCHEMA_POST

        self.assertEqual(
            "CREATE TABLE keywords (\n"
            "  id BINARY(16),\n"
            "  start_ timestamp,\n"
            "  end_ timestamp,\n"
            "  prefixed_start timestamp,\n"
            "  PRIMARY KEY (id)\n"
            ");",
            to_schema(xml))

    def test_to_schema_cornercases(self):
        xml = XML_SCHEMA_PRE + """
              <EntityType Name="Keywords">
                <Key>
                  <PropertyRef Name="ID"/>
                </Key>
                <Property Name="ID" Type="Edm.Guid"/>
                <Property Name="Boolean" Type="Edm.Boolean"/>
                <Property Name="Int16" Type="Edm.Int16"/>
                <Property Name="Int64" Type="Edm.Int64"/>
                <Property Name="Interval" Type="Edm.DateTimeOffset"/>
              </EntityType>
        """ + XML_SCHEMA_POST

        self.assertEqual(
            "CREATE TABLE keywords (\n"
            "  id BINARY(16),\n"
            "  boolean boolean,\n"
            "  int16 smallint,\n"
            "  int64 bigint,\n"
            "  interval timestamp,\n"
            "  PRIMARY KEY (id)\n"
            ");",
            to_schema(xml))

    def test_to_schema_real_world_member_party_to_party_association(self):
        xml = XML_SCHEMA_PRE + """
              <EntityType Name="MemberParty">
                <Key>
                  <PropertyRef Name="ID"/>
                  <PropertyRef Name="Language"/>
                </Key>
                <Property Name="ID" Type="Edm.Int32" Nullable="false"/>
                <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
                <Property Name="PartyNumber" Type="Edm.Int32" Nullable="false"/>
                <Property Name="PartyName" Type="Edm.String" MaxLength="Max" FixedLength="false" Unicode="true"/>
                <Property Name="PersonNumber" Type="Edm.Int32" Nullable="false"/>
                <Property Name="PersonIdCode" Type="Edm.Int32" Nullable="true"/>
                <Property Name="FirstName" Type="Edm.String" MaxLength="40" FixedLength="false" Unicode="true"/>
                <Property Name="LastName" Type="Edm.String" MaxLength="60" FixedLength="false" Unicode="true"/>
                <Property Name="GenderAsString" Type="Edm.String" Nullable="false" MaxLength="1" FixedLength="false" Unicode="false"/>
                <Property Name="PartyFunction" Type="Edm.String" MaxLength="80" FixedLength="false" Unicode="true"/>
                <Property Name="Modified" Type="Edm.DateTime" Precision="3"/>
                <Property Name="PartyAbbreviation" Type="Edm.String" MaxLength="Max" FixedLength="false" Unicode="true"/>
                <NavigationProperty Name="Parties" Relationship="itsystems.Pd.DataServices.DataModel.PartyMemberParty" ToRole="Party" FromRole="MemberParty"/>
                <NavigationProperty Name="MembersCouncil" Relationship="itsystems.Pd.DataServices.DataModel.MemberCouncilMemberParty" ToRole="MemberCouncil" FromRole="MemberParty"/>
              </EntityType>
              <EntityType Name="Party">
                <Key>
                  <PropertyRef Name="ID"/>
                  <PropertyRef Name="Language"/>
                </Key>
                <Property Name="ID" Type="Edm.Int32" Nullable="false"/>
                <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
                <Property Name="PartyNumber" Type="Edm.Int32" Nullable="false"/>
                <Property Name="PartyName" Type="Edm.String" MaxLength="Max" FixedLength="false" Unicode="true"/>
                <Property Name="StartDate" Type="Edm.DateTime" Nullable="false" Precision="3"/>
                <Property Name="EndDate" Type="Edm.DateTime" Precision="3"/>
                <Property Name="Modified" Type="Edm.DateTime" Precision="3"/>
                <Property Name="PartyAbbreviation" Type="Edm.String" MaxLength="Max" FixedLength="false" Unicode="true"/>
                <NavigationProperty Name="MembersParty" Relationship="itsystems.Pd.DataServices.DataModel.PartyMemberParty" ToRole="MemberParty" FromRole="Party"/>
              </EntityType>
              <Association Name="PartyMemberParty">
                <End Type="itsystems.Pd.DataServices.DataModel.Party" Role="Party" Multiplicity="1"/>
                <End Type="itsystems.Pd.DataServices.DataModel.MemberParty" Role="MemberParty" Multiplicity="*"/>
                <ReferentialConstraint>
                  <Principal Role="Party">
                    <PropertyRef Name="ID"/>
                    <PropertyRef Name="Language"/>
                  </Principal>
                  <Dependent Role="MemberParty">
                    <PropertyRef Name="PartyNumber"/>
                    <PropertyRef Name="Language"/>
                  </Dependent>
                </ReferentialConstraint>
              </Association>
        """ + XML_SCHEMA_POST

        self.assertEqual(
            "CREATE TABLE member_party (\n"
            "  id integer NOT NULL,\n"
            "  language char(2) NOT NULL,\n"
            "  party_number integer NOT NULL,\n"
            "  party_name TEXT,\n"
            "  person_number integer NOT NULL,\n"
            "  person_id_code integer,\n"
            "  first_name varchar(40),\n"
            "  last_name varchar(60),\n"
            "  gender_as_string varchar(1) NOT NULL,\n"
            "  party_function varchar(80),\n"
            "  modified timestamp,\n"
            "  party_abbreviation TEXT,\n"
            "  PRIMARY KEY (id, language)\n"
            ");\n"
            "\n"
            "CREATE TABLE party (\n"
            "  id integer NOT NULL,\n"
            "  language char(2) NOT NULL,\n"
            "  party_number integer NOT NULL,\n"
            "  party_name TEXT,\n"
            "  start_date timestamp NOT NULL,\n"
            "  end_date timestamp,\n"
            "  modified timestamp,\n"
            "  party_abbreviation TEXT,\n"
            "  PRIMARY KEY (id, language)\n"
            ");\n"
            "\nALTER TABLE member_party"
            "\nADD CONSTRAINT party_member_party FOREIGN KEY (party_number, language) REFERENCES party (id, language);"
            ,
            to_schema(xml))

    def test_to_schema_real_world_broken_referential_association(self):
        xml = XML_SCHEMA_PRE + """
            <EntityType Name="Session">
                <Key>
                    <PropertyRef Name="ID"/>
                    <PropertyRef Name="Language"/>
                </Key>
                <Property Name="ID" Type="Edm.Int32" Nullable="false"/>
                <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
            </EntityType>
            <EntityType Name="Business">
                <Key>
                    <PropertyRef Name="ID"/>
                    <PropertyRef Name="Language"/>
                </Key>
                <Property Name="ID" Type="Edm.Int32" Nullable="false"/>
                <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
                <Property Name="SubmissionSession" Type="Edm.Int32"/>
            </EntityType>
            <EntityType Name="Vote">
                <Key>
                    <PropertyRef Name="ID"/>
                    <PropertyRef Name="Language"/>
                </Key>
                <Property Name="ID" Type="Edm.Int32" Nullable="false"/>
                <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
            </EntityType>
            <EntityType Name="Meeting">
              <Key>
                <PropertyRef Name="ID"/>
                <PropertyRef Name="Language"/>
              </Key>
              <Property Name="ID" Type="Edm.Int64" Nullable="false"/>
              <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
              <Property Name="IdSession" Type="Edm.Int32"/>
            </EntityType>
            <Association Name="SessionBusiness">
                <End Type="itsystems.Pd.DataServices.DataModel.Session" Role="Session" Multiplicity="1"/>
                <End Type="itsystems.Pd.DataServices.DataModel.Business" Role="Business" Multiplicity="*"/>
                <ReferentialConstraint>
                    <Principal Role="Session">
                        <PropertyRef Name="ID"/>
                        <PropertyRef Name="Language"/>
                    </Principal>
                    <Dependent Role="Business">
                        <PropertyRef Name="Language"/>
                        <PropertyRef Name="SubmissionSession"/>
                    </Dependent>
                </ReferentialConstraint>
            </Association>
            <Association Name="SessionVote">
                <End Type="itsystems.Pd.DataServices.DataModel.Session" Role="Session" Multiplicity="1"/>
                <End Type="itsystems.Pd.DataServices.DataModel.Vote" Role="Vote" Multiplicity="*"/>
                <ReferentialConstraint>
                    <Principal Role="Session">
                        <PropertyRef Name="ID"/>
                        <PropertyRef Name="Language"/>
                    </Principal>
                    <Dependent Role="Vote">
                        <PropertyRef Name="Language"/>
                        <PropertyRef Name="IdSession"/>
                    </Dependent>
                </ReferentialConstraint>
            </Association>
            <Association Name="SessionMeeting">
              <End Type="itsystems.Pd.DataServices.DataModel.Session" Role="Session" Multiplicity="1"/>
              <End Type="itsystems.Pd.DataServices.DataModel.Meeting" Role="Meeting" Multiplicity="*"/>
              <ReferentialConstraint>
                <Principal Role="Session">
                  <PropertyRef Name="ID"/>
                  <PropertyRef Name="Language"/>
                </Principal>
                <Dependent Role="Meeting">
                  <PropertyRef Name="Language"/>
                  <PropertyRef Name="IdSession"/>
                </Dependent>
              </ReferentialConstraint>
            </Association>
        """ + XML_SCHEMA_POST

        self.assertEqual(
            "CREATE TABLE session (\n"
            "  id integer NOT NULL,\n"
            "  language char(2) NOT NULL,\n"
            "  PRIMARY KEY (id, language)\n"
            ");\n"
            "\n"
            "CREATE TABLE business (\n"
            "  id integer NOT NULL,\n"
            "  language char(2) NOT NULL,\n"
            "  submission_session integer,\n"
            "  PRIMARY KEY (id, language)\n"
            ");\n"
            "\n"
            "CREATE TABLE vote (\n"
            "  id integer NOT NULL,\n"
            "  language char(2) NOT NULL,\n"
            "  PRIMARY KEY (id, language)\n"
            ");\n"
            "\n"
            "CREATE TABLE meeting (\n"
            "  id bigint NOT NULL,\n"
            "  language char(2) NOT NULL,\n"
            "  id_session integer,\n"
            "  PRIMARY KEY (id, language)\n"
            ");\n"
            "\nALTER TABLE business"
            "\nADD CONSTRAINT session_business FOREIGN KEY (submission_session, language) REFERENCES session (id, language);"
            "\n\nALTER TABLE vote"
            "\nADD CONSTRAINT session_vote FOREIGN KEY (id_session, language) REFERENCES session (id, language);"
            "\n\nALTER TABLE meeting"
            "\nADD CONSTRAINT session_meeting FOREIGN KEY (id_session, language) REFERENCES session (id, language);"
            ,
            to_schema(xml))


class TestOData(TestCase):
    XML = XML_SCHEMA_PRE + """
        <EntityType Name="Person">
            <Key>
                <PropertyRef Name="ID"/>
                <PropertyRef Name="Language"/>
            </Key>
            <Property Name="ID" Type="Edm.Int32" Nullable="false"/>
            <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
            <Property Name="LastName" Type="Edm.String" MaxLength="60" FixedLength="false" Unicode="true"/>
        </EntityType>
        <EntityType Name="Session">
            <Key>
                <PropertyRef Name="ID"/>
                <PropertyRef Name="Language"/>
            </Key>
            <Property Name="ID" Type="Edm.Int32" Nullable="false"/>
            <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
        </EntityType>
        <EntityType Name="Business">
            <Key>
                <PropertyRef Name="ID"/>
                <PropertyRef Name="Language"/>
            </Key>
            <Property Name="ID" Type="Edm.Int32" Nullable="false"/>
            <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
            <Property Name="SubmissionSession" Type="Edm.Int32"/>
        </EntityType>
        <EntityType Name="Vote">
            <Key>
                <PropertyRef Name="ID"/>
                <PropertyRef Name="Language"/>
            </Key>
            <Property Name="ID" Type="Edm.Int32" Nullable="false"/>
            <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
        </EntityType>
        <EntityType Name="Meeting">
          <Key>
            <PropertyRef Name="ID"/>
            <PropertyRef Name="Language"/>
          </Key>
          <Property Name="ID" Type="Edm.Int64" Nullable="false"/>
          <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
          <Property Name="IdSession" Type="Edm.Int32"/>
        </EntityType>
        <EntityType Name="MeetingNotes">
          <Key>
            <PropertyRef Name="ID"/>
            <PropertyRef Name="Language"/>
          </Key>
          <Property Name="ID" Type="Edm.Int64" Nullable="false"/>
          <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
          <Property Name="Text" Type="Edm.String"/>
        </EntityType>
        <Association Name="SessionBusiness">
            <End Type="itsystems.Pd.DataServices.DataModel.Session" Role="Session" Multiplicity="1"/>
            <End Type="itsystems.Pd.DataServices.DataModel.Business" Role="Business" Multiplicity="*"/>
            <ReferentialConstraint>
                <Principal Role="Session">
                    <PropertyRef Name="ID"/>
                    <PropertyRef Name="Language"/>
                </Principal>
                <Dependent Role="Business">
                    <PropertyRef Name="Language"/>
                    <PropertyRef Name="SubmissionSession"/>
                </Dependent>
            </ReferentialConstraint>
        </Association>
        <Association Name="SessionVote">
            <End Type="itsystems.Pd.DataServices.DataModel.Session" Role="Session" Multiplicity="1"/>
            <End Type="itsystems.Pd.DataServices.DataModel.Vote" Role="Vote" Multiplicity="*"/>
            <ReferentialConstraint>
                <Principal Role="Session">
                    <PropertyRef Name="ID"/>
                    <PropertyRef Name="Language"/>
                </Principal>
                <Dependent Role="Vote">
                    <PropertyRef Name="Language"/>
                    <PropertyRef Name="IdSession"/>
                </Dependent>
            </ReferentialConstraint>
        </Association>
        <Association Name="SessionMeeting">
          <End Type="itsystems.Pd.DataServices.DataModel.Session" Role="Session" Multiplicity="1"/>
          <End Type="itsystems.Pd.DataServices.DataModel.Meeting" Role="Meeting" Multiplicity="*"/>
          <ReferentialConstraint>
            <Principal Role="Session">
              <PropertyRef Name="ID"/>
              <PropertyRef Name="Language"/>
            </Principal>
            <Dependent Role="Meeting">
              <PropertyRef Name="Language"/>
              <PropertyRef Name="IdSession"/>
            </Dependent>
          </ReferentialConstraint>
        </Association>
        <Association Name="PersonMeetingNotes">
          <End Type="itsystems.Pd.DataServices.DataModel.Person" Role="Person" Multiplicity="1"/>
          <End Type="itsystems.Pd.DataServices.DataModel.MeetingNotes" Role="MeetingNotes" Multiplicity="*"/>
          <ReferentialConstraint>
           <Principal Role="Person">
             <PropertyRef Name="ID"/>
             <PropertyRef Name="Language"/>
           </Principal>
           <Dependent Role="MeetingNotes">
              <PropertyRef Name="Language"/>
              <PropertyRef Name="IdSession"/>
            </Dependent>
          </ReferentialConstraint>
        </Association>
        <Association Name="MeetingMeetingNotes">
          <End Type="itsystems.Pd.DataServices.DataModel.Person" Role="Meeting" Multiplicity="1"/>
          <End Type="itsystems.Pd.DataServices.DataModel.MeetingNotes" Role="MeetingNotes" Multiplicity="*"/>
          <ReferentialConstraint>
           <Principal Role="Meeting">
             <PropertyRef Name="ID"/>
             <PropertyRef Name="Language"/>
           </Principal>
           <Dependent Role="MeetingNotes">
              <PropertyRef Name="Language"/>
              <PropertyRef Name="IdSession"/>
            </Dependent>
          </ReferentialConstraint>
        </Association>
    """ + XML_SCHEMA_POST

    def test_str(self):
        o = create_parser(TestOData.XML)
        self.assertEqual("itsystems.Pd.DataServices.DataModel", str(o))
        self.assertEqual("Session", str(o.entity_types[1]))
        self.assertEqual("ID", str(o.entity_types[1].properties[0]))
        self.assertEqual("SessionBusiness", str(o.associations[0]))
        self.assertEqual("Session", str(o.associations[0].principal))

    def test_get_dependencies(self):
        o = create_parser(TestOData.XML)
        entity_type_session = o.entity_types[0]
        self.assertEqual(set(), o.get_dependencies(entity_type_session))

    def test_get_dependants(self):
        o = create_parser(TestOData.XML)
        entity_type_session = o.get_entity_type_by_name("Session")
        entity_type_business = o.get_entity_type_by_name("Business")
        entity_type_vote = o.get_entity_type_by_name("Vote")
        entity_type_meeting = o.get_entity_type_by_name("Meeting")
        entity_type_meeting_notes = o.get_entity_type_by_name("MeetingNotes")
        self.assertEqual({entity_type_business, entity_type_vote, entity_type_meeting, entity_type_meeting_notes},
                         o.get_dependants(entity_type_session))

    def test_topology(self):
        p = create_parser(TestOData.XML)
        t = get_topology(p, p.entity_types)
        self.assertEqual(3, len(t))
        self.assertSetEqual(set([p.get_entity_type_by_name("Session"), p.get_entity_type_by_name("Person")]), t[0])
        self.assertSetEqual(set([p.get_entity_type_by_name("Vote"),
                                 p.get_entity_type_by_name("Business"),
                                 p.get_entity_type_by_name("Meeting")]), t[1])
        self.assertSetEqual(set([p.get_entity_type_by_name("MeetingNotes")]), t[2])

    def test_topology2(self):
        p = create_parser(TestOData.XML)
        t = get_topology(p, [p.get_entity_type_by_name("MeetingNotes")])
        self.assertEqual(3, len(t))
        self.assertSetEqual(set([p.get_entity_type_by_name("Session"), p.get_entity_type_by_name("Person")]), t[0])
        self.assertSetEqual(set([p.get_entity_type_by_name("Meeting")]), t[1])
        self.assertSetEqual(set([p.get_entity_type_by_name("MeetingNotes")]), t[2])
