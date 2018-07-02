package ch.samuelblattner.ffhs.fac.emotica.validator.visitors;

import ch.samuelblattner.ffhs.fac.emotica.common.enums.ValidationState;

public class ValidationFormatter {

    // Statics
    private static final String MSG_VALIDATION_OK = "Validation successful. \uD83E\uDD2A";
    private static final String MSG_VALIDATION_WARNING = "Warning \uD83D\uDE10: Validation was successful with the following warnings:";
    private static final String MSG_VALIDATION_FAIL = "ERROR \uD83D\uDE14: Validation failed due to the following reaons:";
    private static final String MSG_VAR_UNDEFINED = "Variable %s has never been defined or initialized.";
    private static final String MSG_VAR_UNUSED = "Variable %s has been initialized but never used.";

    public static String formatValidationResult(EmoticaValidator.ValidationResult result) {
        if (result.getState() == ValidationState.GOOD_AS_GOLD) {
            return MSG_VALIDATION_OK;
        }
        if (result.getUndefinedVariables().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String varName : result.getUndefinedVariables()) {
                sb.append(String.format("- %s\n", String.format(MSG_VAR_UNDEFINED, varName)));
            }
            return String.format("\n%s\n%s\n\n", MSG_VALIDATION_FAIL, sb.toString());
        }
        if (result.getUnusedVariables().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String varName : result.getUnusedVariables()) {
                sb.append(String.format("- %s\n", String.format(MSG_VAR_UNUSED, varName)));
            }
            return String.format("\n%s\n%s\n\n", MSG_VALIDATION_WARNING, sb.toString());
        }
        return "";
    }
}
