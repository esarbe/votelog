package votelog.client.web.components
import mhtml.Rx
import votelog.domain.politics
import votelog.domain.politics.Language.English

class Language extends html.DynamicSelect[politics.Language]("Language", Rx(politics.Language.values), English)