package View;

import Controller.WeatherParser;
import Model.ThreeHourForecast;
import Model.WeatherData;
import View.UserMarkup;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private static final String NAME_OF_BOT = "astanatest_bot";
    private static final String BOT_TOKEN = "564232572:AAEEKsHMnc5QDOTccmBAcwKwIFhmN_Va_NY";
    private String location;

    private static List<ThreeHourForecast> forecast = new ArrayList<>();

    private WeatherData weatherData;
    private WeatherParser weatherParser;

    public Bot() {
        weatherData = new WeatherData();
        weatherParser = new WeatherParser();
    }

    UserMarkup userMarkup = new UserMarkup();

    private boolean verifyMsg(String s)
    {
        s=s.toLowerCase();
        s=s.replaceAll("\\s","");
        s=s.replaceAll("/", "");
        s=s.replaceAll(",","");
        if(s.matches(".*[^a-z].*")){
            return false;
        }else return true;
    }

    private String showData(final List<ThreeHourForecast> forecast, String msg){
        for(ThreeHourForecast threeHourForecast : forecast){
            msg+="    " +threeHourForecast.getTimeFrom()+" - "+threeHourForecast.getTimeTo()+": ";
            msg+=System.lineSeparator();
            msg+=threeHourForecast.getSky()+", "+threeHourForecast.getTemp()+"°C"+", вологість - "+
                    threeHourForecast.getHumidity()+"%.";
            msg+=System.lineSeparator();
        }
        return msg;
    }


    @Override
    public void onUpdateReceived(Update update) {

        weatherData.cleanAll();

        Message message = update.getMessage();

        if(message.hasText() && verifyMsg(message.getText().toString())){
            if(message.getText().equals("/start")){
                sendMsg(message, "Здравствуйте, "+message.getChat().getFirstName()+"!"+
                        System.lineSeparator()+
                        "\n" +
                        "Для просмотра погоды нужно выбрать локацию." +
                        System.lineSeparator()+
                        "Чтобы выбрать локацию достаточно написать название населенного пункта в чат." +
                        "(\n" +
                        "например: Lviv, UA или New York, USA). " , null);
            }
             else if(message.getText().toString().equals("Weather for today")){
                forecast.clear();
                if(weatherParser.parse(location))
                {
                    forecast = weatherData.getTodayForecast();
                    String msg = "Погода на сегодня - " + forecast.get(0).getDate();
                    msg += " для " + forecast.get(0).getLocation() + ":";
                    msg += System.lineSeparator();
                    msg = showData(forecast, msg);
                    sendMsg(message, msg, userMarkup);
                } else sendMsg(message, "Ошибка. "+location+ " \n" +
                        "не является населенным пунктом. Выберите другой." ,userMarkup );
            }
            else if (message.getText().toString().equals("Weather for tomorrow")) {
                forecast.clear();
                if (weatherParser.parse(location)) {
                    forecast = weatherData.getTomorrowForecast();
                    String msg = "Погода на завтра - " + forecast.get(0).getDate();
                    msg += " для " + forecast.get(0).getLocation() + ":";
                    msg += System.lineSeparator();
                    msg = showData(forecast, msg);
                    sendMsg(message, msg, userMarkup);
                } else sendMsg(message, "Ошибка. " + location + " \n" +
                        "не является населенным пунктом. Выберите другой.", userMarkup);
            }
            else {
                location = message.getText().toString();
                sendMsg(message, "Выбрано локация "+location+"." ,userMarkup );
            }
        }else {
            sendMsg(message, "Ошибка. Такого населенного пункта нет." ,userMarkup );
        }
    }

    private void sendMsg(Message message, String s, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sMsg = new SendMessage();
        sMsg.setChatId(message.getChatId().toString());
        sMsg.setReplyMarkup(replyKeyboardMarkup);
        sMsg.setText(s);
        try {
            sendMessage(sMsg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return NAME_OF_BOT;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}
