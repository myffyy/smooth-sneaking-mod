package ru.myffy.smoothsneak;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

final class SmoothSneakCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "sneaking";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sneaking";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        SmoothSneakGuiOpener.requestOpen();
    }
}
