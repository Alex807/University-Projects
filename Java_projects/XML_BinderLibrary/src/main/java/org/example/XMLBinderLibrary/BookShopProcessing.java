package org.example.XMLBinderLibrary;

import org.example.outputclasses.bookshop.BookShop;
import org.example.outputclasses.bookshop.Bookdata;
import org.example.outputclasses.bookshop.Persondata;

public class BookShopProcessing {

    private static void printPersonData(Persondata person) {
        System.out.println("    Name: " + person.name);
        System.out.println("    Surname: " + person.surname);
        System.out.println("    CV: " + person.cv);
    }

    private static void printBookData(Bookdata book) {
        System.out.println("ID: " + book.id);
        System.out.println("Title: " + book.title);
        System.out.println("Price: " + book.price);
        System.out.println("Description: " + book.description);

        if (!book.author.isEmpty()) {
            System.out.println("Authors:");
            for (int i = 0; i < book.author.size(); i++) {
                Persondata author = book.author.get(i);
                System.out.println("  Author " + (i + 1) + ":");
                printPersonData(author);
            }
        } else {
            System.out.println("No authors listed.");
        }
    }

    public static void printState(BookShop bookShop) {
        System.out.println("\n\t BookShop class");
        System.out.println("=================");

        if (bookShop.book.isEmpty()) {
            System.out.println("No books in the bookshop.");
            return;
        }

        for (int i = 0; i < bookShop.book.size(); i++) {
            Bookdata book = bookShop.book.get(i);
            System.out.println("\nBook " + (i + 1) + ":");
            System.out.println("---------");
            printBookData(book);
        }
    }

    public static void modifyState(BookShop bookShop) {
        // Create first book
        Bookdata book1 = new Bookdata();
        book1.id = "B" + (bookShop.book.size() + 1); //keep indexing
        book1.title = "Java Programming";
        book1.price = 29.99f;
        book1.description = "A comprehensive guide to Java";

        // Add authors to first book
        Persondata author1 = new Persondata();
        author1.name = "John";
        author1.surname = "Doe";
        author1.cv = "Senior Java Developer";
        book1.author.add(author1);

        Persondata author2 = new Persondata();
        author2.name = "Jane";
        author2.surname = "Smith";
        author2.cv = "Software Architect";
        book1.author.add(author2);

        // Add book to bookshop
        bookShop.book.add(book1);

        // Create second book
        Bookdata book2 = new Bookdata();
        book2.id = "B" + (bookShop.book.size() + 1); //keep indexing
        book2.title = "Design Patterns";
        book2.price = 34.99f;
        book2.description = "Essential design patterns in Java";

        // Add author to second book
        Persondata author3 = new Persondata();
        author3.name = "Bob";
        author3.surname = "Wilson";
        author3.cv = "Software Engineer";
        book2.author.add(author3);

        // Add book to bookshop
        bookShop.book.add(book2);
    }
}
