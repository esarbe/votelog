from unittest import TestCase
from odata.odata import _to_snake_case


class Helper(TestCase):
    def test__to_snake_case(self):
        assert _to_snake_case("CamelCase") == "camel_case"
