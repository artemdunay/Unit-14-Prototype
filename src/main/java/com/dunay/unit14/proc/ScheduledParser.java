package com.dunay.unit14.proc;

import com.dunay.unit14.models.Order;
import com.dunay.unit14.models.OrderBuilder;
import com.dunay.unit14.repositories.OrdersRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dunay.unit14.proc.PackageObserver.DIRECTORY_PATH;
import static com.dunay.unit14.proc.PackageObserver.TEMP_DIRECTORY_PATH;


@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public class ScheduledParser {
    private ArrayBlockingQueue<WatchEvent<?>> watchEventsQueue;
    private OrdersRepository repository;
    private final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    final Pattern fileNamePattern = Pattern.compile("([a-zA-Z]*)_(.*)--(.*)\\((\\d*)\\)");

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void scheduledParser() throws InterruptedException, XMLStreamException, IOException {
        if (watchEventsQueue.size() > 0) {
            WatchEvent<?> event = watchEventsQueue.take();
            Order order = mapEventToOrder(event);
            repository.save(order);
        }
    }


    public String copyFileToTempDirectory(String specificDirectoryPath) throws IOException {
        Path source = Paths.get(DIRECTORY_PATH + "\\" + specificDirectoryPath + "\\Public\\ProcJobSummary.xml");
        String targetPath = TEMP_DIRECTORY_PATH + "\\" + specificDirectoryPath + "\\ProcJobSummaryCopy.xml";
        Path targetDirectory = Paths.get(TEMP_DIRECTORY_PATH + "\\" + specificDirectoryPath + "\\");
        Path target = Paths.get(targetPath);
        if (Files.notExists(targetDirectory)) {
            Files.createDirectory(targetDirectory);
        }
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return targetPath;
    }

    public Order mapEventToOrder(WatchEvent<?> event)
            throws IOException, XMLStreamException {
        OrderBuilder builder = new OrderBuilder();
        String specificDirectoryPath = event.context().toString();
        String copiedDirectoryPath = copyFileToTempDirectory(specificDirectoryPath);
        XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(copiedDirectoryPath));
        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isStartElement()) {
                StartElement startElement = nextEvent.asStartElement();
                String localPart = startElement.getName().getLocalPart();
                if (localPart.equals("ProcJobSummary")) {
                    Attribute entryID = startElement.getAttributeByName(QName.valueOf("EntryID"));
                    String id = entryID.getValue();
                    builder = parseEntryID(builder, id);
                }
                if (localPart.equals("DocumentList")) {
                    Attribute symbolicalFileName = startElement.getAttributeByName
                            (QName.valueOf("SymbolicalFileName"));
                    String fileName = symbolicalFileName.getValue();
                    builder = parseSymbolicalFileName(builder, fileName);
                }
            }
            if (nextEvent.isEndElement()) {
                EndElement endElement = nextEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("DocumentList")) {
                    return builder.getResult();
                }
            }
        }
        throw new NoSuchFileException(copiedDirectoryPath);
    }

    public OrderBuilder parseEntryID(OrderBuilder builder, String entryID) {
        //example: EntryID="pj230303-00030"
        builder.setProcId(Integer.parseInt(entryID.substring(9)));
        int year = Integer.parseInt("20" + entryID.substring(2, 4));
        int month = Integer.parseInt(entryID.substring(4, 6));
        int day = Integer.parseInt(entryID.substring(6, 8));
        builder.setDate(LocalDate.of(year, month, day));
        return builder;
    }

    public OrderBuilder parseSymbolicalFileName(OrderBuilder builder, String symbolicalFileName) {
        //example: SymbolicalFileName="Kuzmenko_na_Kolir_23_03_02_B_tir_600_4_0__51_69_75_173_174_174--GR41(4).pdf"
        final Matcher fileNameMatcher = fileNamePattern.matcher(symbolicalFileName);
        if (!fileNameMatcher.lookingAt()) {
            throw new IllegalStateException("There are no matches!");
        }

        builder.setClientName(fileNameMatcher.group(1));
        builder.setFileName(fileNameMatcher.group(2));
        builder.setFormat(fileNameMatcher.group(3));
        builder.setAmount(Integer.parseInt(fileNameMatcher.group(4)));
        return builder;
    }
}
