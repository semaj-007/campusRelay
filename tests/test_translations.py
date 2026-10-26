"""Resource parity and placeholder compatibility for all supported app languages."""
import pathlib
import re
import unittest
import xml.etree.ElementTree as ET

RES = pathlib.Path(__file__).resolve().parents[1] / 'app/src/main/res'

def strings(folder):
    nodes = ET.parse(RES / folder / 'strings.xml').getroot().findall('string')
    result = {node.attrib['name']: ''.join(node.itertext()) for node in nodes}
    assert len(result) == len(nodes), 'Duplicate resource names'
    return result

class TranslationTests(unittest.TestCase):
    def test_every_string_is_translated_with_matching_format_arguments(self):
        english = strings('values')
        for language in ['zu', 'af']:
            translated = strings('values-' + language)
            self.assertEqual(set(english), set(translated), language)
            for key in english:
                self.assertTrue(translated[key].strip(), (language, key))
                self.assertEqual(sorted(re.findall(r'%\d+\$[.\d]*[sdf]', english[key])),
                                 sorted(re.findall(r'%\d+\$[.\d]*[sdf]', translated[key])), (language, key))

if __name__ == '__main__': unittest.main(verbosity=2)
