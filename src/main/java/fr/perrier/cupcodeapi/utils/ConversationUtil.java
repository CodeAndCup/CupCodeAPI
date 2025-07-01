package fr.perrier.cupcodeapi.utils;

import fr.perrier.cupcodeapi.CupCodeAPI;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utility class to facilitate the creation of interactive conversations with players.
 * Allows configuration of prompts, cancel and invalid messages, custom validators,
 * success/cancel callbacks, timeout, and more.
 */
public class ConversationUtil {

    /**
     * Creates a new ConversationBuilder for the given player.
     *
     * @param player The player to start the conversation with.
     * @return A ConversationBuilder to configure the conversation.
     */
    public static ConversationBuilder createConversation(Player player) {
        return new ConversationBuilder(player);
    }

    /**
     * Fluent builder to configure and start a custom conversation with a player.
     */
    public static class ConversationBuilder {
        private final Player player;
        private String promptMessage = "";
        private String cancelMessage = "&cOperation cancelled.";
        private String invalidMessage = "&cInvalid input. Please try again.";
        private Predicate<String> validator = input -> true;
        private Consumer<String> onSuccess;
        private Runnable onCancel;
        private int timeout = 60;
        private boolean closeInventory = true;
        private String escapeSequence = "cancel";

        /**
         * Initializes the builder for the given player.
         *
         * @param player The target player for the conversation.
         */
        public ConversationBuilder(Player player) {
            this.player = player;
        }

        /**
         * Sets the message displayed as the initial prompt.
         *
         * @param message The prompt message.
         * @return This builder.
         */
        public ConversationBuilder withPrompt(String message) {
            this.promptMessage = message;
            return this;
        }

        /**
         * Sets the message displayed when the conversation is cancelled.
         *
         * @param message The cancel message.
         * @return This builder.
         */
        public ConversationBuilder withCancelMessage(String message) {
            this.cancelMessage = message;
            return this;
        }

        /**
         * Sets the message displayed when the input is invalid.
         *
         * @param message The invalid input message.
         * @return This builder.
         */
        public ConversationBuilder withInvalidMessage(String message) {
            this.invalidMessage = message;
            return this;
        }

        /**
         * Sets a custom validator for the user input.
         *
         * @param validator The validation predicate.
         * @return This builder.
         */
        public ConversationBuilder withValidator(Predicate<String> validator) {
            this.validator = validator;
            return this;
        }

        /**
         * Sets the callback to be called on success (valid input).
         *
         * @param onSuccess The consumer called with the valid input.
         * @return This builder.
         */
        public ConversationBuilder onSuccess(Consumer<String> onSuccess) {
            this.onSuccess = onSuccess;
            return this;
        }

        /**
         * Sets the callback to be called on cancellation.
         *
         * @param onCancel The runnable called when cancelled.
         * @return This builder.
         */
        public ConversationBuilder onCancel(Runnable onCancel) {
            this.onCancel = onCancel;
            return this;
        }

        /**
         * Sets the conversation timeout (in seconds).
         *
         * @param seconds The timeout in seconds.
         * @return This builder.
         */
        public ConversationBuilder withTimeout(int seconds) {
            this.timeout = seconds;
            return this;
        }

        /**
         * Specifies whether the player's inventory should be closed at the start of the conversation.
         *
         * @param close true to close the inventory, false otherwise.
         * @return This builder.
         */
        public ConversationBuilder closeInventory(boolean close) {
            this.closeInventory = close;
            return this;
        }

        /**
         * Sets the text sequence that cancels the conversation.
         *
         * @param escapeSequence The cancel sequence.
         * @return This builder.
         */
        public ConversationBuilder withEscapeSequence(String escapeSequence) {
            this.escapeSequence = escapeSequence;
            return this;
        }

        /**
         * Starts the conversation with the configured parameters.
         * Throws IllegalStateException if the onSuccess callback is not set.
         */
        public void start() {
            if (onSuccess == null) {
                throw new IllegalStateException("onSuccess callback must be set");
            }

            if (closeInventory) {
                player.closeInventory();
            }

            new ConversationFactory(CupCodeAPI.getPlugin())
                    .withModality(true)
                    .withPrefix(new NullConversationPrefix())
                    .withFirstPrompt(new StringPrompt() {
                        @Override
                        public @NotNull String getPromptText(@NotNull ConversationContext context) {
                            return ChatUtil.translate(promptMessage + " &7&oEnter &c&o" + escapeSequence + " &7&oto cancel.");
                        }

                        @Override
                        public Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
                            if (input == null || input.equalsIgnoreCase(escapeSequence)) {
                                player.sendMessage(ChatUtil.translate(cancelMessage));
                                if (onCancel != null) {
                                    onCancel.run();
                                }
                                return Prompt.END_OF_CONVERSATION;
                            }

                            if (!validator.test(input)) {
                                player.sendMessage(ChatUtil.translate(invalidMessage));
                                return this;
                            }

                            onSuccess.accept(input);
                            return Prompt.END_OF_CONVERSATION;
                        }
                    })
                    .withLocalEcho(false)
                    .withTimeout(timeout)
                    .buildConversation(player)
                    .begin();
        }
    }

    // Example usage:
    // public static void askForTavernName(Player player, Consumer<String> onNameReceived) {
    //     createConversation(player)
    //         .withPrompt("&aPlease enter a name for your tavern:")
    //         .withValidator(input -> input.length() >= 3)
    //         .withInvalidMessage("&cTavern name must be at least 3 characters long.")
    //         .onSuccess(onNameReceived)
    //         .start();
    // }
}
