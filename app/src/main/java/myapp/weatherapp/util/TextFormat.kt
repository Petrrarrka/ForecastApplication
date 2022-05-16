package myapp.weatherapp.util

class TextFormat {
    companion object {

        fun formatDescription(description: String) =
            description.substring(0, 1).uppercase() + description.substring(1)
    }
}