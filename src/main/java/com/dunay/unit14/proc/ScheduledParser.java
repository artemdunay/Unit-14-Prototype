package com.dunay.unit14.proc;

import com.dunay.unit14.models.Order;
import com.dunay.unit14.repositories.OrdersRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.nio.file.WatchEvent;
import java.time.LocalDate;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dunay.unit14.proc.PackageObserver.DIRECTORY_PATH;


@Component
@AllArgsConstructor
public class ScheduledParser {
    private ArrayBlockingQueue<WatchEvent<?>> watchEventsQueue;
    private OrdersRepository repository;
    private final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    final Pattern fileNamePattern = Pattern.compile("([a-zA-Z]*)_(.*)--(.*)\\((\\d*)\\)");


    @Scheduled(fixedDelay = 5000)
    public void scheduledParser() throws InterruptedException, XMLStreamException, FileNotFoundException, NoSuchFileException {
        if (watchEventsQueue.size() > 0) {
            WatchEvent<?> event = watchEventsQueue.take();
            Order order = mapEventToOrder(event);
            repository.save(order);
        }
    }


    public Order mapEventToOrder(WatchEvent<?> event)
            throws FileNotFoundException, XMLStreamException, NoSuchFileException {
        Order order = new Order();
        String path = DIRECTORY_PATH + "\\" + event.context().toString() + "\\Public\\ProcJobSummary.xml";
        XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(path));
        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isStartElement()) {
                StartElement startElement = nextEvent.asStartElement();
                String localPart = startElement.getName().getLocalPart();
                if (localPart.equals("ProcJobSummary")) {
                    Attribute entryID = startElement.getAttributeByName(QName.valueOf("EntryID"));
                    String id = entryID.getValue();
                    order = parseEntryID(order, id);
                }
                if (localPart.equals("DocumentList")) {
                    Attribute symbolicalFileName = startElement.getAttributeByName
                            (QName.valueOf("SymbolicalFileName"));
                    String fileName = symbolicalFileName.getValue();
                    order = parseSymbolicalFileName(order, fileName);
                }
            }
            if (nextEvent.isEndElement()) {
                EndElement endElement = nextEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("DocumentList")) {
                    return order;
                }
            }
        }
        throw new NoSuchFileException(path);
    }

    public Order parseEntryID(Order order, String entryID) {
        //example: EntryID="pj230303-00030"
        order.setProcId(Integer.parseInt(entryID.substring(9)));
        int year = Integer.parseInt("20" + entryID.substring(2, 4));
        int month = Integer.parseInt(entryID.substring(4, 6));
        int day = Integer.parseInt(entryID.substring(6, 8));
        order.setDate(LocalDate.of(year, month, day));
        return order;
    }

    public Order parseSymbolicalFileName(Order order, String symbolicalFileName) {
        //example: SymbolicalFileName="Kuzmenko_na_Kolir_23_03_02_B_tir_600_4_0__51_69_75_173_174_174--GR41(4).pdf"
        final Matcher fileNameMatcher = fileNamePattern.matcher(symbolicalFileName);
        fileNameMatcher.lookingAt();
        order.setClientName(fileNameMatcher.group(1));
        order.setFileName(fileNameMatcher.group(2));
        order.setFormat(fileNameMatcher.group(3));
        order.setAmount(Integer.parseInt(fileNameMatcher.group(4)));
        return order;
    }
}
