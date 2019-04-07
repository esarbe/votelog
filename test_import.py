from collections import OrderedDict
from unittest import TestCase

from curia_vista import create_parser
from curia_vista_import import fetch_all
from test_schema import XML_SCHEMA_PRE, XML_SCHEMA_POST


class TestResults_to_sql_statement(TestCase):
    # def test_results_to_sql_statement(self):
    #     json_data = {
    #         'results': [
    #             {'ColumnA': 'value a1', 'ColumnB': 'value b1', '__metadata': {"Key": "Do-Not-Use-Me"}},
    #             {'ColumnA': 'value a2', 'ColumnB': None},
    #         ],
    #         '__count': '69415',
    #         '__next': 'loong-string...',
    #     }
    #     self.assertEqual(
    #         "INSERT INTO table (column_b, column_a) VALUES\n"
    #         " (E'value b1', E'value a1'),\n"
    #         " (NULL, E'value a2')\n"
    #         ";",
    #         "\n".join(results_to_sql(json_data['results'], 'table')))

    def test_fetch_all_multipage(self):
        xml = XML_SCHEMA_PRE + """
            <EntityType Name="Table">
                <Key>
                    <PropertyRef Name="ID"/>
                    <PropertyRef Name="Language"/>
                </Key>
                <Property Name="ID" Type="Edm.Int32" Nullable="false"/>
                <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
                <Property Name="Data" Type="Edm.String"/>
            </EntityType>
        """ + XML_SCHEMA_POST
        parser = create_parser(xml)

        def fetcher(url):
            """
            Simple web server mock.
            :param url:
            :return: JSON
            """
            if url == 'https://ws.parlament.ch/odata.svc/Table?$inlinecount=allpages&$select=*':
                return {
                    'd': {
                        'results': [
                            {'ID': '1', 'Language': 'DE', 'Data': 'Data 1'},
                            {'ID': '1', 'Language': 'FR', 'Data': 'Data 2'},
                        ],
                        '__count': '3',
                        '__next': 'Session Page #2',
                    }
                }
            if url == 'Session Page #2':
                return {
                    'd': {
                        'results': [
                            {'ID': '1', 'Language': 'IT', 'Data': None},
                        ],
                        '__count': '3',
                    }
                }
            raise RuntimeError("Unknown URL")

        self.assertEqual(1, len(parser.entity_types))
        result = list(fetch_all(parser.entity_types[0], fetcher))
        result_str = "\n".join(result)
        self.assertEqual(
            "INSERT INTO table (id, language, data) VALUES\n"
            " (E'1', E'DE', E'Data 1'),\n"
            " (E'1', E'FR', E'Data 2'),\n"
            " (E'1', E'IT', NULL);",
            result_str)
