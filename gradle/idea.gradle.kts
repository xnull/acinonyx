import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.gradle.plugins.ide.idea.model.IdeaModel

apply<IdeaPlugin>()

configure<IdeaModel> {
    project {
        languageLevel = IdeaLanguageLevel(JavaVersion.VERSION_1_8)
    }
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}