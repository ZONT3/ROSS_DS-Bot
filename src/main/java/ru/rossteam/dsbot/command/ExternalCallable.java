package ru.rossteam.dsbot.command;

import ru.rossteam.dsbot.tools.Commands;

public interface ExternalCallable {
    void call(Commands.Input input);
}
