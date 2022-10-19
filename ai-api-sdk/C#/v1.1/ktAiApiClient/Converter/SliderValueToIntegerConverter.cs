using System;
using System.Globalization;
using System.Windows.Data;

namespace AiApiTestClient.Converter
{
    public class SliderValueToIntegerConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (value is double)
            {
                double d = (double)value;
                int integer = (int)d;
                return $"{integer}";
            }
            return string.Empty;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
