package jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.model;

import java.util.ArrayList;

public class WeatherModel {
    private WeatherData data;

    public WeatherData getData() {
        return data;
    }

    public void setData(WeatherData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "WeatherModel [data=" + data + "]";
    }

    public class WeatherData {
        private ArrayList<CurrentCondition> current_condition;

        public ArrayList<CurrentCondition> getCurrent_condition() {
            return current_condition;
        }

        public void setCurrent_condition(ArrayList<CurrentCondition> current_condition) {
            this.current_condition = current_condition;
        }

        @Override
        public String toString() {
            String c = "";
            if (1 == current_condition.size()) {
                c = current_condition.get(0).toString();
            }
            return "WeatherData [current_condition=[" + c + "]]";
        }
    }

    public class CurrentCondition {
        private int cloudcover;
        private int humidity;
        private String observation_time;
        private float precipMM;
        private int pressure;
        private int temp_C;
        private int temp_F;
        private int visibility;
        private int weatherCode;
        private ArrayList<WeatherDesc> weatherDesc;
        private ArrayList<WeatherIconUrl> weatherIconUrl;
        private String winddir16Point;
        private int winddirDegree;
        private int windspeedKmph;
        private int windspeedMiles;

        public int getCloudcover() {
            return cloudcover;
        }

        public void setCloudcover(int cloudcover) {
            this.cloudcover = cloudcover;
        }

        public int getHumidity() {
            return humidity;
        }

        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }

        public String getObservation_time() {
            return observation_time;
        }

        public void setObservation_time(String observation_time) {
            this.observation_time = observation_time;
        }

        public float getPrecipMM() {
            return precipMM;
        }

        public void setPrecipMM(float precipMM) {
            this.precipMM = precipMM;
        }

        public int getPressure() {
            return pressure;
        }

        public void setPressure(int pressure) {
            this.pressure = pressure;
        }

        public int getTemp_C() {
            return temp_C;
        }

        public void setTemp_C(int temp_C) {
            this.temp_C = temp_C;
        }

        public int getTemp_F() {
            return temp_F;
        }

        public void setTemp_F(int temp_F) {
            this.temp_F = temp_F;
        }

        public int getVisibility() {
            return visibility;
        }

        public void setVisibility(int visibility) {
            this.visibility = visibility;
        }

        public int getWeatherCode() {
            return weatherCode;
        }

        public void setWeatherCode(int weatherCode) {
            this.weatherCode = weatherCode;
        }

        public ArrayList<WeatherDesc> getWeatherDesc() {
            return weatherDesc;
        }

        public void setWeatherDesc(ArrayList<WeatherDesc> weatherDesc) {
            this.weatherDesc = weatherDesc;
        }

        public ArrayList<WeatherIconUrl> getWeatherIconUrl() {
            return weatherIconUrl;
        }

        public void setWeatherIconUrl(ArrayList<WeatherIconUrl> weatherIconUrl) {
            this.weatherIconUrl = weatherIconUrl;
        }

        public String getWinddir16Point() {
            return winddir16Point;
        }

        public void setWinddir16Point(String winddir16Point) {
            this.winddir16Point = winddir16Point;
        }

        public int getWinddirDegree() {
            return winddirDegree;
        }

        public void setWinddirDegree(int winddirDegree) {
            this.winddirDegree = winddirDegree;
        }

        public int getWindspeedKmph() {
            return windspeedKmph;
        }

        public void setWindspeedKmph(int windspeedKmph) {
            this.windspeedKmph = windspeedKmph;
        }

        public int getWindspeedMiles() {
            return windspeedMiles;
        }

        public void setWindspeedMiles(int windspeedMiles) {
            this.windspeedMiles = windspeedMiles;
        }

        @Override
        public String toString() {
            String d = "";
            if (1 == weatherDesc.size()) {
                d = weatherDesc.get(0).toString();
            }
            String i = "";
            if (1 == weatherIconUrl.size()) {
                i = weatherIconUrl.get(0).toString();
            }
            return "CurrentCondition [cloudcover="
                   + cloudcover
                   + ", humidity="
                   + humidity
                   + ", observation_time="
                   + observation_time
                   + ", precipMM="
                   + precipMM
                   + ", pressure="
                   + pressure
                   + ", temp_C="
                   + temp_C
                   + ", temp_F="
                   + temp_F
                   + ", visibility="
                   + visibility
                   + ", weatherCode="
                   + weatherCode
                   + ", weatherDesc=["
                   + d
                   + "], weatherIconUrl=["
                   + i
                   + "], winddir16Point="
                   + winddir16Point
                   + ", winddirDegree="
                   + winddirDegree
                   + ", windspeedKmph="
                   + windspeedKmph
                   + ", windspeedMiles="
                   + windspeedMiles
                   + "]";
        }
    }

    public class WeatherDesc {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "WeatherDesc [value=" + value + "]";
        }
    }

    public class WeatherIconUrl {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "WeatherIconUrl [value=" + value + "]";
        }

    }
}
